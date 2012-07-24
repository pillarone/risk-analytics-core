hibernate {
    cache.use_second_level_cache=true
    cache.use_query_cache=true
    cache.provider_class='net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings
environments {
	development {
		dataSource {
			dbCreate = "create-drop"
            driverClassName = "org.h2.Driver"
            url = "jdbc:h2:mem:devDB"
		}
	}
	test {
		dataSource {
            dbCreate = "create-drop"
            driverClassName = "org.h2.Driver"
            url = "jdbc:h2:mem:testDB"
		}
	}
	production {
		dataSource {
            dbCreate = "create-drop"
            driverClassName = "org.h2.Driver"
            url = "jdbc:h2:mem:prodDB"
		}
	}
}