node {

    stage 'Checkout'

    git url: 'https://github.com/qwazr/extractor.git'

    stage 'Build' 

    withMaven(maven: 'Maven') {
        sh "mvn -U clean deploy"
    }

    stage 'Docker'
    
    

    stage 'Test'

    withMaven(
        maven: 'Maven',
        mavenSettingsConfig: 'extractor-settings') {
        sh "mvn clean test jacoco:report coveralls:report"
    }

}
