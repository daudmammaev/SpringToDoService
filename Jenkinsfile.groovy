pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        DOCKER_IMAGE = 'your-dockerhub-username/todo-app'
        KUBECONFIG = credentials('k8s-config')
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository -Dmaven.test.failure.ignore=false'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        gitLabConnection('gitlab-connection')
    }

    triggers {
        gitlab(
                triggerOnPush: true,
                triggerOnMergeRequest: true,
                branchFilterType: 'All'
        )
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    currentBuild.displayName = "#${BUILD_NUMBER} - ${env.GIT_BRANCH}"
                    currentBuild.description = "Commit: ${env.GIT_COMMIT.take(8)}"
                }
            }
        }

        stage('Code Quality') {
            parallel {
                stage('Static Analysis') {
                    when {
                        anyOf {
                            branch 'master'
                            branch 'dev'
                            changeRequest()
                        }
                    }
                    steps {
                        sh 'mvn checkstyle:checkstyle'
                        sh 'mvn spotbugs:spotbugs'
                    }
                    post {
                        always {
                            checkstyle pattern: '**/checkstyle-result.xml'
                            recordIssues tools: [spotBugs(pattern: '**/spotbugsXml.xml')]
                        }
                    }
                }

                stage('Dependency Check') {
                    when {
                        anyOf {
                            branch 'master'
                            branch 'dev'
                        }
                    }
                    steps {
                        sh 'mvn org.owasp:dependency-check-maven:check -DskipTests'
                    }
                    post {
                        always {
                            dependencyCheckPublisher pattern: '**/dependency-check-report.html'
                        }
                    }
                }
            }
        }

        stage('Build and Test') {
            parallel {
                stage('Build Application') {
                    steps {
                        sh 'mvn clean compile -DskipTests'
                    }
                }

                stage('Unit Tests') {
                    steps {
                        sh 'mvn test -DskipIntegrationTests=true'
                    }
                    post {
                        always {
                            junit '**/target/surefire-reports/*.xml'
                            publishHTML([
                                    allowMissing: false,
                                    alwaysLinkToLastBuild: true,
                                    keepAll: true,
                                    reportDir: 'target/site/jacoco',
                                    reportFiles: 'index.html',
                                    reportName: 'Unit Test Coverage'
                            ])
                        }
                    }
                }

                stage('Integration Tests') {
                    when {
                        anyOf {
                            branch 'master'
                            branch 'dev'
                        }
                    }
                    steps {
                        sh 'mvn verify -DskipUnitTests=true -Dspring.profiles.active=test'
                    }
                    post {
                        always {
                            junit '**/target/failsafe-reports/*.xml'
                        }
                    }
                }
            }
        }

        stage('Package') {
            when {
                anyOf {
                    branch 'master'
                    branch 'dev'
                    changeRequest()
                }
            }
            steps {
                sh 'mvn package -DskipTests'
                archiveArtifacts 'target/*.jar'
            }
        }

        stage('Build Docker Image') {
            when {
                anyOf {
                    branch 'master'
                    branch 'dev'
                }
            }
            steps {
                script {
                    def commitHash = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    def branchName = env.BRANCH_NAME.replace('/', '-')
                    def imageTag = "${env.DOCKER_IMAGE}:${branchName}-${commitHash}-${BUILD_NUMBER}"

                    // Build Docker image
                    sh "docker build -t ${imageTag} ."

                    // Tag for master branch (latest)
                    if (env.BRANCH_NAME == 'master') {
                        sh "docker tag ${imageTag} ${env.DOCKER_IMAGE}:latest"
                        sh "docker tag ${imageTag} ${env.DOCKER_IMAGE}:${commitHash}"
                    }

                    // Store image tag for deployment
                    env.DOCKER_IMAGE_TAG = imageTag
                }
            }
        }

        stage('Push to Docker Hub') {
            when {
                branch 'master'
            }
            steps {
                script {
                    withCredentials([usernamePassword(
                            credentialsId: 'dockerhub-credentials',
                            usernameVariable: 'DOCKER_USERNAME',
                            passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        sh 'echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin'

                        // Push all tags
                        sh "docker push ${env.DOCKER_IMAGE_TAG}"
                        sh "docker push ${env.DOCKER_IMAGE}:latest"
                        sh "docker push ${env.DOCKER_IMAGE}:${env.GIT_COMMIT.take(8)}"

                        // Cleanup local images
                        sh "docker rmi ${env.DOCKER_IMAGE_TAG} || true"
                        sh "docker rmi ${env.DOCKER_IMAGE}:latest || true"
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            when {
                branch 'master'
            }
            steps {
                script {
                    withKubeConfig([credentialsId: 'k8s-config', serverUrl: '']) {
                        // Update Kubernetes deployment with new image
                        sh """
                            kubectl set image deployment/todo-app todo-app=${env.DOCKER_IMAGE}:latest -n todo-app
                            kubectl rollout status deployment/todo-app -n todo-app --timeout=300s
                        """

                        // Run smoke tests
                        sh '''
                            echo "Running smoke tests..."
                            sleep 30
                            curl -f http://todo.local/actuator/health || exit 1
                            curl -f http://todo.local/api/todos/getAll || exit 1
                        '''
                    }
                }
            }
        }

        stage('Run E2E Tests') {
            when {
                branch 'master'
            }
            steps {
                script {
                    sh 'mvn test -Dtest=*E2ETest -Dspring.profiles.active=kubernetes'
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
    }

    post {
        always {
            // Clean workspace
            cleanWs()

            // Build summary
            script {
                def duration = currentBuild.durationString
                def commit = env.GIT_COMMIT.take(8)
                def message = "Build ${currentBuild.result ?: 'SUCCESS'} - ${duration} - ${commit}"

                emailext (
                        subject: "Build ${currentBuild.result ?: 'SUCCESS'} - ${env.JOB_NAME}",
                        body: """
                    Build: ${env.BUILD_URL}
                    Result: ${currentBuild.result ?: 'SUCCESS'}
                    Duration: ${duration}
                    Commit: ${commit}
                    Branch: ${env.BRANCH_NAME}
                    """,
                        to: 'dev-team@example.com',
                        attachLog: currentBuild.result != 'SUCCESS'
                )
            }
        }

        success {
            script {
                if (env.BRANCH_NAME == 'master') {
                    slackSend(
                            channel: '#deployments',
                            message: "✅ Production deployment successful!\nImage: ${env.DOCKER_IMAGE}:latest\nBuild: ${env.BUILD_URL}"
                    )
                }
            }
        }

        failure {
            script {
                slackSend(
                        channel: '#build-failures',
                        message: "❌ Build failed!\nBranch: ${env.BRANCH_NAME}\nBuild: ${env.BUILD_URL}"
                )
            }
        }

        unstable {
            script {
                slackSend(
                        channel: '#build-warnings',
                        message: "⚠️ Build unstable!\nBranch: ${env.BRANCH_NAME}\nBuild: ${env.BUILD_URL}"
                )
            }
        }
    }
}