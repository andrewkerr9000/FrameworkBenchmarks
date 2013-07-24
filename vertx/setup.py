
import subprocess
import sys
import setup_util
import os
import signal

proc = None

def start(args):
  global proc
  setup_util.replace_text("vertx/src/main/resources/App.groovy", "host: '.*'", "host: '" + args.database_host + "'")

  try:
    proc = subprocess.Popen("stdbuf -oL mvn clean install -DskipTests vertx:runMod", shell=True,stdout=subprocess.PIPE, preexec_fn=os.setsid)

    for data in iter(proc.stdout.readline, ""):
      print data,
      if 'The WebServer has been deployed' in data:
        return 0

    return 1

  except subprocess.CalledProcessError:
    return 1

def stop():
  global proc
  os.killpg(proc.pid, signal.SIGTERM)
  return 0
