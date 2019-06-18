#!/usr/bin/env python3

import subprocess
import sys
import os

# this assumes that all of the .class files are in a 'bin' directory
# relative to this file

project_scope = os.path.dirname(os.path.realpath(__file__))

new_argv = ["java", "-cp", project_scope + "/bin", '-ea', "commandLine.Main"] + sys.argv[1:]
subprocess.run(new_argv)
