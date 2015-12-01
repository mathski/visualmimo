#!/usr/bin/python3
# A desktop client to the embedding server, because HTML5 is hard.

import time
import subprocess
from threading import Thread

FILENAME = '/tmp/vmimo.avi'
URL = 'http://107.170.136.219'
STRING = 'abcdefghijk'
IMAGEID = '8'

status = -1

def update_video():
    url = URL + '/encode/' + IMAGEID + '/' + STRING
    print('fetching %s...' % url)
    subprocess.check_call(['wget', '-O', FILENAME, url])

    kill_video()
    
    print('playing %s' % FILENAME)
    subprocess.check_call(['mplayer', '-loop', '0', FILENAME])

def kill_video():
    if subprocess.call(['killall', 'mplayer']):
        print('Error killing mplayer.')

def check_status():
    # TODO: check if data is stale
    # TODO: replace polling with socket.io
    # print('Checking status...')
    return 0

if __name__ == "__main__":
    while True:
        new_status = check_status()
        if new_status != status:
            status = new_status
            t = Thread(target=update_video)
            t.start()
        time.sleep(1)