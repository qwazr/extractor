node {

    stage 'Checkout'

    git url: 'https://github.com/qwazr/extractor.git'

    stage 'Build' 

    withMaven(maven: 'Maven') {
        sh "mvn -U clean deploy"
    }

    stage 'Test'

    withMagen(maven: 'Maven') {
        sh "mvn clean test jacoco:report coveralls:report"
    }

}
