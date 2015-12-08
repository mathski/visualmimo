#!/usr/bin/python3
# Trivial flask server to wrap MATLAB encoder and serve output
# Remember to `source set_ld_path.sh` before running.

from flask import Flask, send_file, render_template
import os.path
import subprocess
import vmimo.server

s = vmimo.server.initialize()

app = Flask(__name__)

@app.route('/')
def root():
	return 'access /encode/<imageid>/<asciistring>'

@app.route('/encode/<string:imageid>/<string:asciistring>')
def encode(imageid, asciistring):
	filename = imageid + '-' + asciistring + '.avi'
	filename_webm = imageid + '-' + asciistring + '.webm'

	print('encoding %s' % filename)

	fullpath = os.path.join('..', filename)
	fullpath_webm = os.path.join('..', filename_webm)

	if not os.path.isfile(filename):
		print(os.listdir())
		print('file (%s) not found, generating...' % filename)
		s.sample(imageid, asciistring)
		print('...generated!')

	# note that I'm passing unsanitized input as parameters.
	# This is astoundingly stupid, so never deploy this anywhere important.
	command = 'LD_LIBRARY_PATH=/usr/lib ffmpeg -i %s %s -nostdin -y -hide_banner -nostats' % (filename, filename_webm)
	print(command)
	subprocess.call(command, shell=True)


	return send_file(fullpath_webm, mimetype='video/webm')

@app.route('/view/<string:imageid>/<string:asciistring>')
def view(imageid, asciistring):
	return render_template('server.html', imageid=imageid, asciistring=asciistring)



if __name__ == '__main__':
	print('starting!')
	app.debug = True
	#app.use_x_sendfile = True
	app.run()