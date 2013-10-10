package org.pillarone.riskanalytics.core.parameterization

interface IMultiDimensionalConstraints extends Serializable{

    boolean matches(int row, int column, Object value)

    String getName()

    Class getColumnType(int column)

    boolean emptyComponentSelectionAllowed(int column)
    
    /**
     * @param marker should be an interface extending IComponentMarker
     * @return the column number containing component names of components implementing the
     *              marker interface {@link org.pillarone.riskanalytics.core.components.IComponentMarker}
     *         or <tt>null</tt> if no column is found
     */
    Integer getColumnIndex(Class marker)

}