# prokzio
Zio gRPC/REST HTTP Proxy for Kafka


### Install GraalVM on Mac with Honebrew & jenv
`brew install --cask graalvm/tap/graalvm-ce-java11`

#### add to ~/.zshrc
`export GRAALVM_HOME="/Library/Java/JavaVirtualMachines/graalvm-ce-java11-21.0.0/Contents/Home/"`

#### allow to execute graalvm
`sudo xattr -r -d com.apple.quarantine /Library/Java/JavaVirtualMachines/graalvm-ce-java11-21.0.0`

#### install native-image tool
`/Library/Java/JavaVirtualMachines/graalvm-ce-java11-21.0.0/Contents/Home/bin/gu install native-image`

#### install espresso
`/Library/Java/JavaVirtualMachines/graalvm-ce-java11-21.0.0/Contents/Home/bin/gu install espresso`

#### add to jenv
`jenv add /Library/Java/JavaVirtualMachines/graalvm-ce-java11-21.0.0/Contents/Home`

#### enable export
`jenv enable-plugin export`

#### set jenv
`jenv local graalvm64-11.0.10`

Before running sbt
#### 
`exec $SHELL -l`
