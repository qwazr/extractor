node {

    stage 'Checkout'

    git url: 'https://github.com/qwazr/extractor.git'

    stage 'Build' 

    withMaven(maven: 'Maven') {
        sh "mvn -U clean deploy"
    }

    stage 'Test'

    env.PATH = "${tool 'Maven'}/bin:${env.PATH}"
    sh "mvn clean test jacoco:report coveralls:report"

}