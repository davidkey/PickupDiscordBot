node {
	stage ('Checkout') {
		checkout scm 
	}

	stage('Apply Permissions') {
		sh 'pwd'
		sh 'chmod +x mvnw'
	}

	stage('Build') {
		sh "./mvnw clean package -DskipTests=true"
		step([$class: 'ArtifactArchiver', artifacts: '**/target/*.jar', fingerprint: true])
	}
   
   	post {
		cleanup {
			deleteDir()
		}
	}
}