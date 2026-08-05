#!/bin/bash
# 在 Git Bash 中手动启动 IDEA 内置 Maven 3.9.9（规避 classworlds 启动器 ClassNotFound）
# 用法：./bmvn.sh package -DskipTests
set -e

MAVEN_HOME_WIN='D:\IDEA\IntelliJ IDEA 2025.1\plugins\maven\lib\maven3'
JAVA_HOME_WIN='C:\Users\Administrator\.jdks\ms-21.0.12'

MAVEN_HOME="$(cygpath -m "$MAVEN_HOME_WIN")"
JAVA_HOME="$(cygpath -m "$JAVA_HOME_WIN")"
export JAVA_HOME="$JAVA_HOME"

exec "$JAVA_HOME/bin/java" \
  -classpath "$MAVEN_HOME/boot/plexus-classworlds-2.8.0.jar" \
  -Dclassworlds.conf="$MAVEN_HOME/bin/m2.conf" \
  -Dmaven.home="$MAVEN_HOME" \
  -Dmaven.multiModuleProjectDirectory="$(cygpath -m "$(pwd)")" \
  org.codehaus.plexus.classworlds.launcher.Launcher "$@"
