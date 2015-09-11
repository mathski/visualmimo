#!/usr/bin/python
import os
import signal
import subprocess
import random

MATLAB_COMMAND = './run_randomMessageSample.sh'
MATLAB_LOCATION = '/usr/local/MATLAB/R2013a/'

processes = {}

def runMatlab(imageId, instanceId):
	if instanceId in processes:
		os.killpg(processes[instanceId].pid, signal.SIGTERM)
	processes[instanceId] = subprocess.Popen([MATLAB_COMMAND, MATLAB_LOCATION, imageId, instanceId], cwd='../matlab/', preexec_fn=os.setsid)

# Returns id from id set closest to query.
def getNearestId(query, ids):
	def str2bin(st):
		return ''.join(map(lambda x: format(x, 'b'),bytearray(st,'ascii')))
	return max(zip([len(list(filter(lambda z: z[0] == z[1], zip(query, str2bin(id))))) for id in ids], ids))[1]

photos = list(map(str, range(1, 15)))
ids = ['scgrhjk', '89z03ne']


from flask import Flask
app = Flask(__name__)

@app.route('/')
def main():
	return 'Visual MIMO Photo Album running.'

@app.route('/change/<string:query>')
def change(query):
	id = getNearestId(query, ids)
	photo = random.choice(photos)
	runMatlab(photo, id)
	return 'Setting photo %s on instance %s' % (photo, id)

@app.route('/init')
def reset():
	for id in ids:
		runMatlab('5', id)
	return 'init'


if __name__ == '__main__':
	app.debug = True
	app.run()