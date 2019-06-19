#!/usr/bin/env python3

import subprocess
import sys

new_argv = ["java", "-cp", '-ea', './bin/', "commandLine.Main"] + sys.argv[1:]
subprocess.run(new_argv)
