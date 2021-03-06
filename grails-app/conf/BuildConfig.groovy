grails.project.work.dir = 'target'

grails.project.dependency.resolver = 'maven'
grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		mavenLocal() // Note: use 'grails maven-install' to install required plugins locally
		grailsCentral()
		mavenCentral()
		mavenRepo 'http://ec2-35-170-59-132.compute-1.amazonaws.com:8080/artifactory/libs-snapshots'
		mavenRepo 'http://ec2-35-170-59-132.compute-1.amazonaws.com:8080/artifactory/libs-releases'
		mavenRepo 'http://ec2-35-170-59-132.compute-1.amazonaws.com:8080/artifactory/plugins-releases'
		mavenRepo 'http://ec2-35-170-59-132.compute-1.amazonaws.com:8080/artifactory/plugins-snapshots'
		mavenRepo 'https://repo.transmartfoundation.org/content/repositories/public/'
	}

	dependencies {
		compile 'org.transmartproject:transmart-core-api:18.1-SNAPSHOT'

		test 'org.hamcrest:hamcrest-library:1.3'
		test 'org.hamcrest:hamcrest-core:1.3'

		if (enableClover) {
			compile "com.atlassian.clover:clover:$CLOVER_VERSION", {
				export = false
			}
		}

		test 'org.gmock:gmock:0.9.0-r435-hyve2', {
			transitive = false
			export = false
		}

		test 'org.grails:grails-datastore-test-support:1.0.2-grails-2.4'
	}

	plugins {

		compile ':hibernate:3.6.10.19', { export = false }
		compile ':transmart-core:18.1-SNAPSHOT'

		build ':release:3.1.2', ':rest-client-builder:2.1.1', {
			export = false
		}

		if (enableClover) {
			compile ":clover:$CLOVER_VERSION", {
				export = false
			}
		}
	}
}

final String CLOVER_VERSION = '4.0.1'
def enableClover = System.getenv('CLOVER')

if (enableClover) {
	grails.project.fork.test = false

	clover {
		on = true

		srcDirs = ['../src/java', '../src/groovy', '../grails-app', 'test/unit', 'test/integration']

		// work around bug in compile phase in groovyc; see CLOV-1466 and GROOVY-7041
		excludes = ['**/ClinicalDataTabularResult.*']

		reporttask = { ant, binding, plugin ->
			String reportDir = "$binding.projectTargetDir/clover/report"
			ant.'clover-report' {
				ant.current(outfile: reportDir, title: 'transmart-core-db') {
					format(type: 'html', reportStyle: 'adg')
					testresults(dir: 'target/test-reports', includes: '*.xml')
					ant.columns {
						lineCount()
						filteredElements()
						uncoveredElements()
						totalPercentageCovered()
					}
				}
				ant.current(outfile: "$reportDir/clover.xml") {
					format(type: 'xml')
					testresults(dir: 'target/test-reports', includes: '*.xml')
				}
			}
		}
	}
}
