pipeline {
	agent any
    tools {
        jdk "jdk8u292-b10"
    }
	
	stages {
		stage('Clean') {
			steps {
				echo 'Cleaning Project'
				sh 'chmod +x gradlew'
				sh './gradlew clean'
			}
		}

		stage('Build') {
			steps {
				echo 'Building'
				sh './gradlew build'
			}
		}
	}

	post {
		always {
			archiveArtifacts '*/build/libs/**.jar'
		}
	}
}
