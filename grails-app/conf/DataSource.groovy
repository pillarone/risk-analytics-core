dataSource {
	pooled = true
	driverClassName = "org.hsqldb.jdbcDriver"
	username = "sa"
	password = ""
}
hibernate {
    cache.use_second_level_cache=true
    cache.use_query_cache=true
    cache.provider_class='net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings
environments {
	development {
		dataSource {
            driverClassName = "org.h2.Driver"
            dbCreate = "create-drop" // one of 'create', 'create-drop','update'
			url = "jdbc:h2:mem:devDb"
		}
	}
	test {
		dataSource {
            driverClassName = "org.h2.Driver"
            dbCreate = "update"
			url = "jdbc:h2:mem:devDb"
		}
	}
	production {
		dataSource {
            driverClassName = "org.h2.Driver"
            dbCreate = "update"
			url = "jdbc:h2:mem:devDb"
		}
	}
}