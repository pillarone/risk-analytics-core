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
  Matching occurred in these ways:
   - term found in item name
   - term exactly matched item's creator
   - term found in any of item's tags (for taggable items - Simulations, Parameterizations and Resources)
   - Parameterizations: additionally, term found in status, or exact match to its numeric deal id
   - Simulation: additionally, term found in name of its Pn or Result Template

   and yielded all items matching on any of the above tests on any of the OR terms.

   New :-

   + Filter terms can restrict their effect to the Name, State, Tags, Deal Id, and Owner fields.
   + Filtering can successively narrow a search via AND clauses
   + Negative filtering allows to exclude specific values via ! prefix
   + Match allowed on partial username (it can be hard to remember exact usernames)
   + Allow finding simulation results, in addition to finding P4ns, for given (exact) deal id

   This has been extended as described below.

   Note: The implementation is not very 'object oriented' and might fit nicely into some kind of visitor
   pattern implementation if some bright spark can see how to do that without doubling the size of the
   code (or doubling the filtering time :-P ) (or both :P :P)
   -----------------------------------------

   + FILTER ON SPECIFIC FIELDS

        Terms can focused on a specific field by prefixing with one of (case not sensitive):
            DEALID:, NAME:, OWNER:, STATE:, or TAG:
        (Shorter forms D:, N:, O:, S:, and T: are allowed too.)

        This refinement decays to the old behaviour if keywords are not used.

        If term begins with "<keyword>:" and <keyword> is relevant to the item type being matched, use it as now.
        (Ditto if term doesn't begin with "<keyword>:")
        Else force it to fail matching the item.

        E.g. originally the search text:  "review"
            yielded items with the word 'review' in the name or with status 'in review'.

        Now, the search text: "status:review"
            will only yield items with status 'in review';
            items with 'review' in the *name* but in status 'data entry' will not match.


   + ALLOW AND-ing MULTIPLE FILTERS

        After  i) is implemented, this is easy to layer on top of existing design.

        Formerly the filter 'review OR production' made sense (matching on names or status values),
        whereas filter 'review AND production' could not make sense for status values (an item has only one status).
        Now, the filter 'name:review AND name:production' makes sense and helps refine the search.

        The implementation is somewhat simplistic but useful as long as the query doesn't get too clever:
        The query is initially split into successive restriction filters at AND boundaries.
        Each restriction is then split into alternative search terms at OR boundaries.
        Each filter's terms are applied to the results of the prior filter, successively shrinking the matching tree.

   + ALLOW NEGATIVE FILTERS TO EXCLUDE ITEMS

        Eg The filter 'tag:Q4 2013 AND !tag:Allianz Re' would be useful for listing all the non AZRE models in the Q4 quarter run.

   + ALLOW EXACT MATCH IN ADDITION TO CONTAINMENT MATCH

        Using the = operator instead of the : allows specifying an *exact* match.

        So whereas the filter : 't:50K'
        would match tags :  '50K' and '14Q1v11_50K',
        the filter : 't=50K'
        would only match the tag '50K'.
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
        if( item == null ){
            false
        }
        if( matchTerms == null || matchTerms.empty ){
            true
        }
        return matchTerms.every { passesRestriction(item, it) }
    }

    static boolean passesRestriction(CacheItem item, String[] matchTerms) {

        return FilterHelp.matchName(item, matchTerms) ||
                FilterHelp.matchOwner(item, matchTerms) ||
                internalAccept(item, matchTerms)
    }

    private static boolean internalAccept(CacheItem item, String[] searchStrings) {
        LOG.warn("CacheItem IGNORED by AllFieldsFilter: ${item.nameAndVersion} ")
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

    // This override renders Batches immune to filtering (so they always appear).
    // Set -DfilterBatchesInGUI=true to remove this immunity.
    //
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
    // Santa's not-so-little helper..
    //
    static class FilterHelp {

        private static Log LOG = LogFactory.getLog(FilterHelp)

        //Search term can begin with a column prefix to restrict its use against a specific 'column'
        static final String dealIdPrefix = "DEALID:"
        static final String namePrefix = "NAME:"
        static final String ownerPrefix = "OWNER:"
        static final String statePrefix = "STATE:"
        static final String tagPrefix = "TAG:"

        static final String dealIdPrefixEq = "DEALID="
        static final String namePrefixEq = "NAME="
        static final String ownerPrefixEq = "OWNER="
        static final String statePrefixEq = "STATE="
        static final String tagPrefixEq = "TAG="
        static final String nonePrefix = "";

        //Shorter versions of the above for more concise filter expressions
        static final String dealIdShort = "D:"
        static final String nameShort = "N:"
        static final String ownerShort = "O:"
        static final String stateShort = "S:"
        static final String tagShort = "T:"

        static final String dealIdShortEq = "D="
        static final String nameShortEq = "N="
        static final String ownerShortEq = "O="
        static final String stateShortEq = "S="
        static final String tagShortEq = "T="

        // TODO Should enforce excluding these special characters from item fields (name, tags, status etc)
        //
        static final String FILTER_NEGATION = "!"
        static final String COLON = ":"
        static final String EQUALS = "="

        //In expected *most-frequently-used-first* order (need for speed):
        //
        static final String[] columnFilterPrefixes = [
                tagPrefix, tagShort,
                namePrefix, nameShort,
                statePrefix, stateShort,
                ownerPrefix, ownerShort,
                dealIdPrefix, dealIdShort,

                tagPrefixEq, tagShortEq,
                namePrefixEq, nameShortEq,
                statePrefixEq, stateShortEq,
                ownerPrefixEq, ownerShortEq,
                dealIdPrefixEq, dealIdShortEq
        ];

        // Search terms without a column prefix apply to all columns
        private static boolean isGeneralSearchTerm(String term) {
            return nonePrefix == getColumnFilterPrefix(term);
        }

        //Check if one of the legal values forms prefix.
        //(Copes with extra spaces after ! or before :)
        private static String getColumnFilterPrefix(String term) {
            int colonIndex = term.indexOf(COLON);
            int equalsIndex = term.indexOf(EQUALS);
            if (colonIndex == -1 && equalsIndex == -1) { // not a column-specific term
                return nonePrefix;
            }
            if (colonIndex != -1 && equalsIndex != -1) { // ambiguous - treat as generic search term
                return nonePrefix;
            }

            int bangIndex = term.indexOf(FILTER_NEGATION);

            //Give up if : or = precedes ! - it's a weirdo, treat as a general search term
            if (colonIndex < bangIndex || equalsIndex < bangIndex ) {
                LOG.warn("getColumnFilterPrefix(term: ${term}): Bang after ':' or '=' - treat as generic filter.")
                return nonePrefix;
            }

            //Column prefix sits between any bang and the colon/equals
            String prefix = (colonIndex  != -1) ? term.substring(bangIndex + 1, colonIndex).trim() + COLON
                          : (equalsIndex != -1) ? term.substring(bangIndex + 1, colonIndex).trim() + EQUALS
                          : ""

            return columnFilterPrefixes.find { prefix.equalsIgnoreCase(it) } ?: nonePrefix;

//            LOG.warn("getColumnFilterPrefix(term: ${term})-> ignoring unknown prefix: ${prefix}")
//            return nonePrefix;
        }

        //Acceptor terms either have no prefix or prefix matches the column in question and ends in ':'
        //
        private static boolean isDealIdAcceptor(String term) {
            if (term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(EQUALS)) return false
            return [nonePrefix, dealIdShort, dealIdPrefix].any { it == prefix }
        }

        private static boolean isNameAcceptor(String term) {
            if (term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(EQUALS)) return false
            return [nonePrefix, nameShort, namePrefix].any { it == prefix }
        }

        private static boolean isOwnerAcceptor(String term) {
            if (term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(EQUALS)) return false
            return [nonePrefix, ownerShort, ownerPrefix].any { it == prefix }
        }

        private static boolean isStateAcceptor(String term) {
            if (term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(EQUALS)) return false
            return [nonePrefix, stateShort, statePrefix].any { it == prefix }
        }

        private static boolean isTagAcceptor(String term) {
            if (term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(EQUALS)) return false
            return [nonePrefix, tagShort, tagPrefix].any { it == prefix }
        }

        //Column-equals-operator terms have prefix matches the column in question and end in '='
        //
        private static boolean isDealIdEqualsOp(String term) {
            if (term.startsWith(FILTER_NEGATION) ) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(COLON)) return false
            return [/*nonePrefix,*/ dealIdShort, dealIdPrefix].any { it == prefix }
        }

        private static boolean isNameEqualsOp(String term) {
            if (term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(COLON)) return false
            return [nonePrefix, nameShort, namePrefix].any { it == prefix }
        }

        private static boolean isOwnerEqualsOp(String term) {
            if (term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(COLON)) return false
            return [nonePrefix, ownerShort, ownerPrefix].any { it == prefix }
        }

        private static boolean isStateEqualsOp(String term) {
            if (term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(COLON)) return false
            return [nonePrefix, stateShort, statePrefix].any { it == prefix }
        }

        private static boolean isTagEqualsOp(String term) {
            if (term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(COLON)) return false
            return [nonePrefix, tagShort, tagPrefix].any { it == prefix }
        }


        // Rejector terms begin with "!", match the column in question, and end in ':'
        //
        private static boolean isDealIdRejector(String term) {
            if (!term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(EQUALS)) return false
            return [dealIdShort, dealIdPrefix].any { it == prefix }
        }

        private static boolean isNameRejector(String term) {
            if (!term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(EQUALS)) return false
            return [nameShort, namePrefix].any { it == prefix }
        }

        private static boolean isOwnerRejector(String term) {
            if (!term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(EQUALS)) return false
            return [ownerShort, ownerPrefix].any { it == prefix }
        }

        private static boolean isStateRejector(String term) {
            if (!term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(EQUALS)) return false
            return [stateShort, statePrefix].any { it == prefix }
        }

        private static boolean isTagRejector(String term) {
            if (!term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(EQUALS)) return false
            return [tagShort, tagPrefix].any { it == prefix }
        }

        // Column-not-equals terms begin with "!", match the column in question, and end in '='
        //
        private static boolean isDealIdNotEqualsOp(String term) {
            if (!term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(COLON)) return false
            return [dealIdShort, dealIdPrefix].any { it == prefix }
        }

        private static boolean isNameNotEqualsOp(String term) {
            if (!term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(COLON)) return false
            return [nameShort, namePrefix].any { it == prefix }
        }

        private static boolean isOwnerNotEqualsOp(String term) {
            if (!term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(COLON)) return false
            return [ownerShort, ownerPrefix].any { it == prefix }
        }

        private static boolean isStateNotEqualsOp(String term) {
            if (!term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(COLON)) return false
            return [stateShort, statePrefix].any { it == prefix }
        }

        private static boolean isTagNotEqualsOp(String term) {
            if (!term.startsWith(FILTER_NEGATION)) return false;
            String prefix = getColumnFilterPrefix(term)
            if (prefix.endsWith(COLON)) return false
            return [tagShort, tagPrefix].any { it == prefix }
        }


        private static boolean matchName(CacheItem item, String[] matchTerms) {
            return matchTerms.any {
                          isNameAcceptor(it)    ?  StringUtils.containsIgnoreCase(item.nameAndVersion, getText(it))
                        : isNameRejector(it)    ? !StringUtils.containsIgnoreCase(item.nameAndVersion, getText(it))
                        : isNameEqualsOp(it)    ?  StringUtils.equalsIgnoreCase(item.nameAndVersion, getText(it))
                        : isNameNotEqualsOp(it) ? !StringUtils.equalsIgnoreCase(item.nameAndVersion, getText(it))
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
                }
                else
                if (isTagRejector(it)){
                    //e.g. term '!tag:Q4 2013' will match any sim or pn tagged 'H2 2013' (but not 'Q4 2013 ReRun')
                     !item.tags*.name.any { String tag -> StringUtils.containsIgnoreCase(tag, getText(it)) }

                }
                else
                if (isTagEqualsOp(it)) {
                    //e.g. term 'tag=Q4 2013' will match any sim or pn tagged 'Q4 2013' (but not eg 'Q4 2013 ReRun')
                    item.tags*.name.any { String tag -> StringUtils.equalsIgnoreCase(tag, getText(it)) }
                }
                else
                if (isTagNotEqualsOp(it)) {
                    //e.g. term '!tag = Q4 2013' will match any sim or pn tagged 'Q1 2015' (but also eg 'Q4 2013 ReRun')
                    !item.tags*.name.any { String tag -> StringUtils.equalsIgnoreCase(tag, getText(it)) }
                }
                else{
                    //e.g. term 'status:Q4 2013' would fail to match a sim tagged Q4 2013 (and status 'in review' etc)
                    false
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

