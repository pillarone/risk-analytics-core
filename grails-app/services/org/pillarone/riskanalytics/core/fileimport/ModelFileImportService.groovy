package org.pillarone.riskanalytics.core.fileimport

import org.pillarone.riskanalytics.core.ModelDAO
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.filter.RegexPatternTypeFilter
import java.util.regex.Pattern
import org.springframework.beans.factory.config.BeanDefinition
import org.pillarone.riskanalytics.core.model.MigratableModel
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.util.ClassPathScanner

/**
 * @author sebastian.cartier (at) intuitive-collaboration (dot) com
 */
class ModelFileImportService extends FileImportService {

    private String fileName
    private String versionNumber
    private Class modelClass

    final String fileSuffix = "Model"

    protected boolean saveItemObject(String srcCode) {
        ModelDAO dao = new ModelDAO()
        dao.name = fileName
        dao.srcCode = srcCode
        dao.itemVersion = versionNumber
        dao.modelClassName = modelClass.name

        return dao.save() != null
    }

    public getDaoClass() {
        ModelDAO
    }

    public String prepare(URL file, String itemName) {
        fileName = itemName - '.groovy'

        modelClass = findModelClass()

        versionNumber = Model.getModelVersion(modelClass).toString()

        return fileName
    }

    @Override
    protected boolean lookUpItem(String itemName) {
        ModelDAO.findByModelClassNameAndItemVersion(modelClass.name, versionNumber) != null
    }



    protected Class findModelClass() {
        ClassPathScanner scanner = new ClassPathScanner()
        scanner.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile('(.*)\\.' + fileName + '$')))
        Set<BeanDefinition> components = scanner.findCandidateComponents("")
        if (components.size() > 1) {
            throw new IllegalStateException("More than one model class found with name $fileName: ${components.collect { it.beanClassName }.join(" ")}")
        } else if (components.empty) {
            throw new IllegalStateException("Model class not found for $fileName")
        } else {
            return getClass().getClassLoader().loadClass(components.toList()[0].beanClassName)
        }
    }
}