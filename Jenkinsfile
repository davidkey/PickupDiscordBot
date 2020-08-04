pipeline {
	agent any

	stages {
		stage ('Checkout') {
			steps {
				checkout scm 
			}
		}
	
		stage('Apply Permissions') {
			steps {
				sh 'pwd'
				sh 'chmod +x mvnw'
			}
		}
	
		stage('Build') {
			steps {
				sh "./mvnw clean package -DskipTests=true"
				step([$class: 'ArtifactArchiver', artifacts: '**/target/*.jar', fingerprint: true])
			}
		}
	}
	
	post {
		cleanup {
			deleteDir()
		}
	}
}