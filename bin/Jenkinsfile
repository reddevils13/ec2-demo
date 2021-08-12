def MICROSERVICE_NAME    = scm.getUserRemoteConfigs()[0].getUrl().tokenize('/').pop().toLowerCase().replace('.git', '') // Name of the Microservice (Repository Name)
def SONAR_SERVER_NAME    = 'SonarQube batch6' // SonarQube Server Name defined in Jenkins Configuration
def SONAR_PROJECT_KEY    = 'batch6' // SonarQube Project Key
def QUALITY_GATE_TIMEOUT = 10 // Quality Gate timeout time in minutes
def AWS_REGION           = 'us-east-1' // AWS Region for deployment
def RECIPIENT_EMAIL      = null // Email Address of Recipient (Update if required)

def qualityGateResult    = null // Quality Gate Result
def sonarProjectUrl      = null // SonarQube Project Dashboard URL

pipeline {
  agent any

  options {
    buildDiscarder(logRotator(numToKeepStr: '4', daysToKeepStr: '7', artifactDaysToKeepStr: '7', artifactNumToKeepStr: '4'))
  }

  stages {
    stage ('Compile') {
      steps {
        sh 'mvn compile'
      }
    }
    stage ('Test') {
      steps {
        sh 'mvn clean test'
      }
    }
    stage ('Quality Analysis') { // SonarQube Code Quality Analysis Stage
      steps {
        // Use SonarQube Server Env with name SONAR_SERVER_NAME defined in Jenkins Configuration
        withSonarQubeEnv(SONAR_SERVER_NAME) {
          // Maven command for SonarQube Code Quality Analysis (hostUrl and login token are defined in Jenkins Config)
          sh "mvn sonar:sonar -Dsonar.projectKey=${SONAR_PROJECT_KEY}.${MICROSERVICE_NAME} -Dsonar.projectName=${SONAR_PROJECT_KEY}.${MICROSERVICE_NAME}"
          script {
            sonarProjectUrl = "${env.SONAR_HOST_URL}/dashboard?id=${SONAR_PROJECT_KEY}"
          }
        }
      }
    }
    stage('Quality Gate') { // SonarQube Quality Gate Stage works only when Webhook enabled
      steps {
        // Timeout will abort pipeline job after specified time
        // Useful when Webhook not recieved
        timeout(time: QUALITY_GATE_TIMEOUT, unit: 'MINUTES') {
          // Runs the groovy script as a step
          script {
            // Waits for Quality Gate Results through Webhook
            qualityGateResult = waitForQualityGate()
            println qualityGateResult.status // Quality Gate Status (Can be used for conditional equations)
            if (qualityGateResult.status != 'OK') {
              error 'Qulaity Gate Failed'
            }
          }
        }
      }
    }
    stage ('Package') {
      steps {
        sh 'mvn package'
      }
    }
    stage ('Build') { // Docker Image Build stage
      steps {
        sh "docker build . -t ${MICROSERVICE_NAME}"
      }
    }
    stage ('Deploy') { // Docker Image Deploy stage (push Image to AWS ECR)
      steps {
        withCredentials([string(credentialsId: 'AWS_ECR_ID', variable: 'AWS_ECR_ID')]) {
          sh "aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${AWS_ECR_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
          sh "docker tag ${MICROSERVICE_NAME}:latest ${AWS_ECR_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${MICROSERVICE_NAME}:${BUILD_NUMBER}"
          sh "docker push ${AWS_ECR_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${MICROSERVICE_NAME}:${BUILD_NUMBER}"
        }
      }
    }
  }
  post {
    always {
      // Send Notification on any build status (SUCCESS, FAILURE, UNSTABLE)
      sendNotification(RECIPIENT_EMAIL, sonarProjectUrl)
    }
  }
}

def sendNotification(email, projectUrl) {
  def buildName = "Job '${JOB_NAME}-${BUILD_NUMBER}'"
  def buildTime = currentBuild.duration / 1000
  def subject = "${currentBuild.currentResult}: ${buildName}"
  def body = "Build Time: ${buildTime}s\nBuild URL: ${BUILD_URL}\nJob URL: ${JOB_URL}"
  def color = (currentBuild.currentResult == 'SUCCESS') ? 'good' : 'danger'

  if (projectUrl != null) {
    body += '\nSonarQube Project URL: ' + projectUrl
  }

  if (email != null) {
    emailext to: email, subject: subject, body: body
  }
}
