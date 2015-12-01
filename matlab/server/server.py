#!/bin/python3
# Trivial flask server to wrap MATLAB encoder and serve output
# Remember to `source set_ld_path.sh` before running.

from tornado.wsgi import WSGIContainer
from tornado.httpserver import HTTPServer
from tornado.ioloop import IOLoop

from flask import Flask, send_file, render_template
import os.path
import vmimo.server

s = vmimo.server.initialize()

app = Flask(__name__)

@app.route('/')
def root():
	return 'access /encode/<imageid>/<asciistring>'

@app.route('/dummy')
def dummy():
	return send_file('drop.avi', mimetype='video/avi')

@app.route('/encode/<string:imageid>/<string:asciistring>')
def encode(imageid, asciistring):
	filename = imageid + '-' + asciistring + '.avi'

	print('encoding %s' % filename)

	fullpath = os.path.join('..', filename)
	# fullpath = filename

	if not os.path.isfile(filename):
		# print(os.listdir())
		print('file (%s) not found, generating...' % filename)
		s.sample(imageid, asciistring)
		print('...generated!')


	return send_file(fullpath, mimetype='video/avi')

@app.route('/view/<string:imageid>/<string:asciistring>')
def view(imageid, asciistring):
	return render_template('server.html', imageid=imageid, asciistring=asciistring)



if __name__ == '__main__':
	app.debug = True
	app.use_x_sendfile = True
	# app.run(threaded=True)

	http_server = HTTPServer(WSGIContainer(app))
	http_server.listen(5000)
	IOLoop.instance().start()