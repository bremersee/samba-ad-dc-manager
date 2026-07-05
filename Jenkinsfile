pipeline {
  agent {
    label 'jdk21&&maven&&docker'
  }
  environment {
    CODECOV_TOKEN = credentials('samba-ad-dc-manager-codecov-token')
    TEST = true
    SITE = false
    DEPLOY_TO_DEBIAN_REPO = true
    APT_INSTALL_ON_DC1 = false
    APT_INSTALL_ON_DC2 = false
    CLEAN = true
  }
  tools {
    jdk 'jdk21'
    maven 'm3'
  }
  options {
    buildDiscarder(logRotator(numToKeepStr: '8', artifactNumToKeepStr: '8'))
  }
  stages {
    stage('Tools') {
      steps {
        sh 'java -version'
        sh 'mvn -B --version'
      }
    }
    stage('Test') {
      when {
        environment name: 'TEST', value: 'true'
      }
      steps {
        sh 'mvn -B -P build-system clean test'
      }
      post {
        always {
          junit '**/surefire-reports/*.xml'
          recordCoverage(
              tools: [[parser: 'JACOCO', pattern: '**/coverage-reports/*.exec']],
              sourceCodeRetention: 'LAST_BUILD'
          )
        }
      }
    }
    stage('Site') {
      when {
        allOf {
          environment name: 'SITE', value: 'true'
          anyOf {
            branch 'develop'
            branch 'main'
            branch 'feature/*'
            branch 'bugfix/*'
          }
        }
      }
      steps {
        sh '''
          mvn -B -P build-system,gh-pages-site site site:stage
          git clone -b gh-pages git@github.com:bremersee/samba-ad-dc-manager.git target/gh-pages
          cp -rf target/staging/* target/gh-pages
          git -C target/gh-pages add .
          git -C target/gh-pages commit -m "Maven site"
          git -C target/gh-pages push
          rm -rf target/gh-pages
        '''
      }
      post {
        always {
          sh 'curl -s https://codecov.io/bash | bash -s - -t ${CODECOV_TOKEN}'
        }
      }
    }
    stage('Deploy to Debian Repo') {
      when {
        allOf {
          environment name: 'DEPLOY_TO_DEBIAN_REPO', value: 'true'
          anyOf {
            branch 'develop'
            branch 'main'
            branch 'bugfix/*'
          }
        }
      }
      steps {
        sh '''
          mvn -B -P build-system,debian,deploy-to-debian-repo clean deploy
        '''
      }
    }
    stage('Apt Install on DC1') {
      when {
        allOf {
          environment name: 'APT_INSTALL_ON_DC1', value: 'true'
          anyOf {
            branch 'develop'
            branch 'main'
            branch 'bugfix/*'
          }
        }
      }
      steps {
        sh '''
          mvn -B -DskipTests=true -P build-system,apt-install-on-dc1 deploy
        '''
      }
    }
    stage('Apt Install on DC2') {
      when {
        allOf {
          environment name: 'APT_INSTALL_ON_DC2', value: 'true'
          anyOf {
            branch 'develop'
            branch 'main'
            branch 'bugfix/*'
          }
        }
      }
      steps {
        sh '''
          mvn -B -DskipTests=true -P build-system,apt-install-on-dc2 deploy
        '''
      }
    }
    stage('Clean') {
      when {
        environment name: 'CLEAN', value: 'true'
      }
      steps {
        sh 'mvn -B clean'
      }
    }
  }
}