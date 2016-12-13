node {

    stage 'Checkout'

    git url: 'https://github.com/qwazr/extractor.git'

    stage 'Build' 

    withMaven(maven: 'Maven') {
        sh "mvn -U clean deploy"
    }

    stage 'Docker'
    
    

    stage 'Test'

    env.PATH = "${tool 'Maven'}/bin:${env.PATH}"

    configFileProvider(
            [configFile(fileId: 'extractor-settings', variable: 'MAVEN_SETTINGS')]) {
            sh 'mvn -s $MAVEN_SETTINGS clean test jacoco:report coveralls:report'
    }

}
