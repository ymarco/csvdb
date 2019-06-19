#!/usr/bin/env python3

import subprocess
import sys
import os

# this assumes that the executable jar file is in out/artifacts/master_jar/master.jar
# relative to this file's path

project_scope = os.path.dirname(os.path.realpath(__file__))
jarpath = os.path.join(project_scope, "out", "artifacts", "master_jar", "master.jar")
heapsize = "-Xmx8g"

new_argv = ["java", "-jar", '-ea', heapsize, jarpath, ] + sys.argv[1:]
subprocess.run(new_argv)
