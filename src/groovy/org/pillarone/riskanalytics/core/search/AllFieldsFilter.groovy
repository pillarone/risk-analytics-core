package org.pillarone.riskanalytics.core.search

import org.apache.commons.lang.StringUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.modellingitem.BatchCacheItem
import org.pillarone.riskanalytics.core.modellingitem.CacheItem
import org.pillarone.riskanalytics.core.modellingitem.ParameterizationCacheItem
import org.pillarone.riskanalytics.core.modellingitem.ResourceCacheItem
import org.pillarone.riskanalytics.core.modellingitem.SimulationCacheItem

/* frahman 2014-01-02 Extended filtering syntax introduced.

  Originally :-

  AllFieldsFilter supported one or more _alternative_ search terms separated by OR.
  Matching was attempted in following ways:
   - term found in item name
   - term exactly matches item's creator
   - term found in any of item's tags (for taggable items - Simulations, Parameterizations and Resources)
   - Parameterizations: additionally, term found in status, or exact match to Pn's deal (numeric) id
   - Simulation items: additionally, term found in name of its Pn or Result Template

   and yielded all items matching on any of the above tests on any of the OR terms.

   New :-

   + Filter terms can now be restricted in their scope
   + Filtering can successively narrow a search via AND clauses
   + Negative filtering allows to exclude specific values via ! prefix
   + Match allowed on partial username (its hard to remember exact usernames)
   + Allow finding simulation results for given (exact) deal id, in addition to finding P4ns

   This has been extended as described below.
   The implementation is not particularly 'object oriented' and would probably fit into some kind of visitor
   pattern implementation if some bright spark can see how to do that without doubling the size of the code.
   -----------------------------------------

   + ALLOW RESTRICTED FILTER/SEARCH TERM SCOPE

        Terms can be restricted to apply to a specific item attribute by prefixing with one of:
            DEALID:|NAME:|OWNER:|STATE:|TAG:

        This is a refinement that decays to current behaviour if keywords are not used.

        If term begins with "<keyword>:" and <keyword> is relevant to the item type being matched, use it as now.
        (Ditto if term doesn't begin with "<keyword>:")
        Else force it to fail matching the item.

        E.g. originally the search text:  "review"
            yielded items with the word 'review' in the name or with status 'in review'.

        Now, the search text: "status:review"
            will only yield items with the status.  Pns with 'review' in the name but status 'in production' will not match.


   + ALLOW AND-ing MULTIPLE FILTERS

        After  i) is implemented, this is easy to layer on top of existing design.

        Currently the filter 'review OR production' makes sense for names or status values.
        But 'review AND production' does not make sense for status values (an item has only one status).
        But the filter 'name:review AND name:production' would make sense and help refine the search.

        The implementation is somewhat simplistic but useful as long as the query doesn't get too clever:
        The query is split into successive filters at AND boundaries.
        Each filter is then split into alternative search terms at OR boundaries.
        Each filter's terms are applied to the results of the prior filter, narrowing the search.

   + ALLOW NEGATIVE FILTERS TO EXCLUDE ITEMS
        Eg The filter 'tag:Q4 2013 AND !tag:Allianz Re' would be useful for listing all the non AZRE models in the Q4 quarter run.
*/

class AllFieldsFilter implements ISearchFilter {

    protected static Log LOG = LogFactory.getLog(AllFieldsFilter)
    private static
    final boolean matchSimulationResultsOnDealId = System.getProperty("matchSimulationResultsOnDealId", "true").equalsIgnoreCase("true");

    // It can be counterproductive to filter batches.  You search for p14ns to put into a batch but the batch name
    // is not likely to match the same filter exactly, so you can't see the batch once it's created. So by default
    // we don't filter batches.  Matthias may come up with a friendly toggle in the GUI; meanwhile I need to test..
    private static final boolean filterBatchesInGUI = System.getProperty("filterBatchesInGUI", "false").equalsIgnoreCase("true");


    static final String AND_SEPARATOR = " AND "
    static final String OR_SEPARATOR = " OR "
    String query = ""

    List<String[]> matchTerms;

    // Add setter to avoid splitting query string for each item ( > 3K items )
    //
    void setQuery(String q) {
        LOG.debug("*** Splitting up filter : " + q)
        query = q
        String[] restrictions = query.split(AND_SEPARATOR)
        LOG.debug("Restrictions (AND clauses): " + restrictions)
        if (matchTerms == null) {
            matchTerms = new ArrayList<>();
        }
        matchTerms.clear()
        restrictions.each {
            String[] orArray = it.split(OR_SEPARATOR)
            orArray.each { String bit -> bit = bit.trim() }
            LOG.debug("Terms (OR clauses): " + orArray)
            matchTerms.add(orArray)
        }
    }

    @Override
    boolean accept(CacheItem item) {
        return item != null && matchTerms.every { passesRestriction(item, it) }
    }

    static boolean passesRestriction(CacheItem item, String[] matchTerms) {

        return FilterHelp.matchName(item, matchTerms) ||
                FilterHelp.matchOwner(item, matchTerms) ||
                internalAccept(item, matchTerms)
    }

    private static boolean internalAccept(CacheItem item, String[] searchStrings) {
        return false
    }

    //  Useful to match results on dealid off the pn though, for quarter runs.
    private static boolean internalAccept(SimulationCacheItem sim, String[] matchTerms) {
        return FilterHelp.matchTags(sim, matchTerms) ||
                matchTerms.any {
                    FilterHelp.isNameAcceptor(it) &&
                            (StringUtils.containsIgnoreCase(sim.parameterization?.name, FilterHelp.getText(it)) ||
                                    StringUtils.containsIgnoreCase(sim.resultConfiguration?.name, FilterHelp.getText(it))
                            )
                } ||
                (
                        //Can disable this to check performance impact..
                        //
                        matchSimulationResultsOnDealId && FilterHelp.matchDealId(sim.parameterization, matchTerms)
                )
    }

    // This override forces all Batch nodes to be visible in tree
    private static boolean internalAccept(BatchCacheItem b, String[] matchTerms) {
        return !filterBatchesInGUI;
    }

    //matchTerms.any { isStateAcceptor(it) && StringUtils.containsIgnoreCase(p14n.status?.toString(), getText(it)) } ||
    //matchTerms.any { isDealIdAcceptor(it)&& StringUtils.equalsIgnoreCase(p14n.dealId?.toString(),   getText(it)) }
    private static boolean internalAccept(ParameterizationCacheItem p14n, String[] matchTerms) {
        return FilterHelp.matchTags(p14n, matchTerms) ||
                FilterHelp.matchState(p14n, matchTerms) ||
                FilterHelp.matchDealId(p14n, matchTerms);
    }

    private static boolean internalAccept(ResourceCacheItem res, String[] matchTerms) {
        return FilterHelp.matchTags(res, matchTerms);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Santa's not so little helper..
    //
    static class FilterHelp {

        private static Log LOG = LogFactory.getLog(FilterHelp)

        //Search term can begin with a column prefix to restrict its use against a specific 'column'
        static final String dealIdPrefix = "DEALID:"
        static final String namePrefix = "NAME:"
        static final String ownerPrefix = "OWNER:"
        static final String statePrefix = "STATE:"
        static final String tagPrefix = "TAG:"
        static final String nonePrefix = "";

        //Shorter versions of the above for more concise filter expressions
        static final String dealIdShort = "D:"
        static final String nameShort = "N:"
        static final String ownerShort = "O:"
        static final String stateShort = "S:"
        static final String tagShort = "T:"

        static final String FILTER_NEGATION = "!"
        static final String COLON = ":"
//      static final boolean canNamesHaveColons = System.getProperty("AllFieldsFilter.canNamesHaveColons","false").toBoolean();

        //In expected most-frequently-used-first order:
        static final String[] columnFilterPrefixes = [tagPrefix, tagShort,
                namePrefix, nameShort,
                statePrefix, stateShort,
                ownerPrefix, ownerShort,
                dealIdPrefix, dealIdShort];

        // Search terms without a column prefix apply to all columns
        private static boolean isGeneralSearchTerm(String term) {
            return nonePrefix == getColumnFilterPrefix(term);
        }

        //Check if one of the legal values forms prefix.
        //(Copes with extra spaces after ! or before :)
        private static String getColumnFilterPrefix(String term) {
            int colonIndex = term.indexOf(COLON);
            if (colonIndex == -1) {
                return nonePrefix;
            }

            int bangIndex = term.indexOf(FILTER_NEGATION);

            //Give up if : precedes ! - it's a weirdo, treat as a general search term
            if (colonIndex < bangIndex) {
                LOG.warn("getColumnFilterPrefix(term: ${term}): Bang after colon - treat as generic filter.")
                return nonePrefix;
            }

            //Column filter prefix sits between any bang and the colon
            String prefix = term.substring(bangIndex + 1, colonIndex).trim() + COLON;

            return columnFilterPrefixes.find { prefix.equalsIgnoreCase(it) } ?: nonePrefix;

//            LOG.warn("getColumnFilterPrefix(term: ${term})-> ignoring unknown prefix: ${prefix}")
//            return nonePrefix;
        }

        //Acceptor terms either have no prefix or prefix matches the column in question
        //
        private static boolean isDealIdAcceptor(String term) {
            if (term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            return [nonePrefix, dealIdShort, dealIdPrefix].any { it == prefix }
        }

        private static boolean isNameAcceptor(String term) {
            if (term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            return [nonePrefix, nameShort, namePrefix].any { it == prefix }
        }

        private static boolean isOwnerAcceptor(String term) {
            if (term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            return [nonePrefix, ownerShort, ownerPrefix].any { it == prefix }
        }

        private static boolean isStateAcceptor(String term) {
            if (term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            return [nonePrefix, stateShort, statePrefix].any { it == prefix }
        }

        private static boolean isTagAcceptor(String term) {
            if (term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            return [nonePrefix, tagShort, tagPrefix].any { it == prefix }
        }
        // Rejector terms begin with ! and must match the column in question
        //
        private static boolean isDealIdRejector(String term) {
            if (!term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            return [dealIdShort, dealIdPrefix].any { it == prefix }
        }

        private static boolean isNameRejector(String term) {
            if (!term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            return [nameShort, namePrefix].any { it == prefix }
        }

        private static boolean isOwnerRejector(String term) {
            if (!term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            return [ownerShort, ownerPrefix].any { it == prefix }
        }

        private static boolean isStateRejector(String term) {
            if (!term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            return [stateShort, statePrefix].any { it == prefix }
        }

        private static boolean isTagRejector(String term) {
            if (!term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            return [tagShort, tagPrefix].any { it == prefix }
        }


        private static boolean matchName(CacheItem item, String[] matchTerms) {
            return matchTerms.any {
                isNameAcceptor(it) ? StringUtils.containsIgnoreCase(item.nameAndVersion, getText(it))
                        : isNameRejector(it) ? !StringUtils.containsIgnoreCase(item.nameAndVersion, getText(it))
                        : false
            };
        }

        private static boolean matchOwner(CacheItem item, String[] matchTerms) {
            return matchTerms.any {
                isOwnerAcceptor(it) ? StringUtils.containsIgnoreCase(item.creator?.username, getText(it))
                        : isOwnerRejector(it) ? !StringUtils.containsIgnoreCase(item.creator?.username, getText(it))
                        : false
            };
        }

        private static boolean matchState(ParameterizationCacheItem p14n, String[] matchTerms) {
            return matchTerms.any {
                isStateAcceptor(it) ? StringUtils.containsIgnoreCase(p14n.status?.toString(), getText(it))
                        : isStateRejector(it) ? !StringUtils.containsIgnoreCase(p14n.status?.toString(), getText(it))
                        : false
            };
        }

        private static boolean matchDealId(ParameterizationCacheItem p14n, String[] matchTerms) {
            return matchTerms.any {
                isDealIdAcceptor(it) ? StringUtils.equalsIgnoreCase(p14n?.dealId?.toString(), getText(it))
                        : isDealIdRejector(it) ? !StringUtils.equalsIgnoreCase(p14n?.dealId?.toString(), getText(it))
                        : false
            };
        }

        // Only call this for things that have tags (Simulation, Parameterization or Resource)
        private static boolean matchTags(def item, String[] matchTerms) {
            return matchTerms.any { def it ->
                if (isTagAcceptor(it)) {
                    //e.g. term 'tag:Q4 2013' will match any sim or pn tagged 'Q4 2013' (but also eg 'Q4 2013 ReRun')
                    item.tags*.name.any { String tag -> StringUtils.containsIgnoreCase(tag, getText(it)) }
                } else {
                    //e.g. term '!tag:Q4 2013' will match any sim or pn tagged 'H2 2013' (but not 'Q4 2013 ReRun')
                    isTagRejector(it) ? !item.tags*.name.any { String tag -> StringUtils.containsIgnoreCase(tag, getText(it)) }

                            //e.g. term 'status:Q4 2013' would fail to match a sim tagged Q4 2013 (and status 'in review' etc)
                            : false
                }
            }
        }

        // Drop any column prefix to return unadorned text user wanted to filter with
        private static String getText(String specificSearchTerm) {
            String raw = specificSearchTerm.replaceFirst("(?i)^[!]? *[a-z]+ *:", ""); //case insensitive regex-replace
            LOG.debug("getTargetText(${specificSearchTerm}-->${raw})")

            return raw.trim()
        }

    }

}

