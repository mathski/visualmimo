var nes = new JSNES({'emulateSound': false, 'ui': $('#emulator').JSNESUI({"ROM": [["Super Mario Bros.", "supermario.nes"]]})});


var socket = io();

socket.emit('joinRoom', {room: 0});

socket.on('keyDown', function(data) {
    console.log('keydown:', data);
    nes.keyboard.keyDown({keyCode: data.keyCode});
});

socket.on('keyUp', function(data) {
    nes.keyboard.keyUp({keyCode: data.keyCode});
});

