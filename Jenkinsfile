pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        AWS_REGION = 'us-east-1'
        AWS_ACCOUNT_ID = '707360336863'
        ECR_REPOSITORY = 'order-service'
        SONAR_HOST_URL = 'http://54.80.253.206:9000'
        SONAR_PROJECT_KEY = 'order-service'
        COVERAGE_THRESHOLD = '80.0'
        GITOPS_REPOSITORY = 'git@github.com:ankitm99/dock-kube-tut-gitops.git'
        GITOPS_CREDENTIALS_ID = 'git-ssh'
        SONAR_CREDENTIALS_ID = 'SonarToken'
    }

    stages {
        stage('Test and Package') {
            steps {
                dir('order-service') {
                    sh 'echo "JAVA_HOME=$JAVA_HOME"; java -version; mvn -version'
                    sh 'mvn -B clean verify'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('order-service') {
                    withCredentials([string(credentialsId: "${SONAR_CREDENTIALS_ID}", variable: 'SONAR_TOKEN')]) {
                        sh '''
                            mvn -B sonar:sonar \
                              -Dsonar.projectKey="$SONAR_PROJECT_KEY" \
                              -Dsonar.host.url="$SONAR_HOST_URL" \
                              -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                        '''
                    }
                }
            }
        }

        stage('Check Code Coverage') {
            steps {
                withCredentials([string(credentialsId: "${SONAR_CREDENTIALS_ID}", variable: 'SONAR_TOKEN')]) {
                    script {
                        def coverage = sh(
                            script: '''
                                set -eu
                                for attempt in $(seq 1 24); do
                                    response=$(curl --fail --silent --show-error \
                                      --user "$SONAR_TOKEN:" \
                                      "$SONAR_HOST_URL/api/measures/component?component=$SONAR_PROJECT_KEY&metricKeys=coverage")
                                    value=$(printf '%s' "$response" \
                                      | jq -r '.component.measures[]? | select(.metric == "coverage") | .value // empty' \
                                      | head -n 1)
                                    if [ -n "$value" ]; then
                                        printf '%s' "$value"
                                        exit 0
                                    fi
                                    sleep 5
                                done
                                echo "SonarQube coverage measure was not available after waiting." >&2
                                exit 1
                            ''',
                            returnStdout: true
                        ).trim().toDouble()

                        echo "Coverage: ${coverage}%"
                        if (coverage < env.COVERAGE_THRESHOLD.toDouble()) {
                            error "Coverage is below the ${env.COVERAGE_THRESHOLD}% threshold."
                        }
                    }
                }
            }
        }

        stage('Build and Push Image to ECR') {
            steps {
                script {
                    env.IMAGE_TAG = env.BUILD_NUMBER
                    env.ECR_REGISTRY = "${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_REGION}.amazonaws.com"
                    env.IMAGE_URI = "${env.ECR_REGISTRY}/${env.ECR_REPOSITORY}:${env.IMAGE_TAG}"
                }
                sh '''
                    set -eu
                    aws ecr get-login-password --region "$AWS_REGION" \
                      | docker login --username AWS --password-stdin "$ECR_REGISTRY"
                    docker build \
                      --platform linux/amd64 \
                      --tag "$IMAGE_URI" \
                      --file order-service/Dockerfile \
                      order-service
                    docker push "$IMAGE_URI"
                '''
            }
        }

        stage('Update Image Tag in GitOps') {
            steps {
                sshagent(credentials: [env.GITOPS_CREDENTIALS_ID]) {
                    sh '''
                        set -eu
                        rm -rf -- gitops
                        git clone "$GITOPS_REPOSITORY" gitops
                        cd gitops
                        VALUES_FILE="helm/microservices-app/values-prod.yaml"
                        test -f "$VALUES_FILE"

                        awk '
                            /^orderService:/ { in_section = 1; next }
                            /^[^[:space:]]/ { in_section = 0 }
                            in_section && /^[[:space:]]+tag:/ { found++ }
                            END { exit found != 1 }
                        ' "$VALUES_FILE" || {
                            echo "Expected exactly one orderService image tag in $VALUES_FILE" >&2
                            exit 1
                        }

                        sed -i.bak -E \
                          '/^orderService:/,/^[^[:space:]]/ s/^([[:space:]]+tag:[[:space:]]*).*/\\1'"$IMAGE_TAG"'/' \
                          "$VALUES_FILE"
                        rm -f "$VALUES_FILE.bak"

                        git config user.name "Jenkins"
                        git config user.email "jenkins@users.noreply.github.com"
                        git diff --check
                        if git diff --quiet -- "$VALUES_FILE"; then
                            echo "GitOps image tag is already up to date."
                            exit 0
                        fi
                        git add "$VALUES_FILE"
                        git commit -m "Deploy order-service ${IMAGE_TAG}"
                        git push origin HEAD
                    '''
                }
            }
        }
    }

    post {
        always {
            sh 'docker logout "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com" >/dev/null 2>&1 || true'
        }
    }
}
