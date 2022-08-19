# Install Nexus using OperatorHub
```yaml
apiVersion: sonatype.com/v1alpha1
kind: NexusRepo
metadata:
  name: example-nexusrepo
  namespace: nexus
spec:
  config:
    enabled: false
    mountPath: /sonatype-nexus-conf
  deployment:
    annotations: {}
    terminationGracePeriodSeconds: 120
  route:
    enabled: false
    name: docker
    portName: docker
  secret:
    enabled: false
    mountPath: /etc/secret-volume
    readOnly: true
  ingress:
    annotations: {}
    enabled: false
    path: /
    tls:
      enabled: true
      secretName: nexus-tls
  service:
    annotations: {}
    enabled: false
    labels: {}
    ports:
      - name: nexus-service
        port: 80
        targetPort: 80
  statefulset:
    enabled: false
  replicaCount: 1
  deploymentStrategy: {}
  nexusProxyRoute:
    enabled: false
  tolerations: []
  persistence:
    accessMode: ReadWriteOnce
    enabled: true
    storageSize: 20Gi
  nexus:
    nexusPort: 8081
    dockerPort: 5003
    resources: {}
    imageName: >-
      registry.connect.redhat.com/sonatype/nexus-repository-manager@sha256:bf4200653ad59c50b87788265b2f12c9da6942413e2487c24e4d5407c44ad598
    readinessProbe:
      failureThreshold: 6
      initialDelaySeconds: 180
      path: /
      periodSeconds: 30
    livenessProbe:
      failureThreshold: 6
      initialDelaySeconds: 180
      path: /
      periodSeconds: 30
    env:
      - name: INSTALL4J_ADD_VM_PARAMS
        value: >-
          -Xms5000M -Xmx5000M -XX:MaxDirectMemorySize=5000M
          -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap
          -Djava.util.prefs.userRoot=/nexus-data/javaprefs
      - name: NEXUS_SECURITY_RANDOMPASSWORD
        value: 'false'
    securityContext: {}
    imagePullSecret: ''
    imagePullPolicy: IfNotPresent
    service:
      type: NodePort
    hostAliases: []
    podAnnotations: {}
```
### Configure sonar as maven proxy.
https://blog.sonatype.com/using-nexus-3-as-your-repository-part-1-maven-artifacts

### Configure a external registry with Nexus
https://tomd.xyz/openshift-nexus-docker-registry/

Don't forget to create a edge route to pull the images

