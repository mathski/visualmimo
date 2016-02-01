document.body.addEventListener('touchmove', function(event) {
    event.preventDefault();
}, false); 

var socket = io();

var room = 0;

socket.emit('joinRoom', {room: room});

document.getElementById('nes-a').addEventListener('touchstart', function(event) {
    socket.emit('keyDown', {room: room, keyCode: 88});
});

document.getElementById('nes-a').addEventListener('touchend', function(event) {
    socket.emit('keyUp', {room: room, keyCode: 88});
});


document.getElementById('nes-b').addEventListener('touchstart', function(event) {
    socket.emit('keyDown', {room: room, keyCode: 90});
});

document.getElementById('nes-b').addEventListener('touchend', function(event) {
    socket.emit('keyUp', {room: room, keyCode: 90});
});

document.getElementById('nes-up').addEventListener('touchstart', function(event) {
    socket.emit('keyDown', {room: room, keyCode: 38});
});

document.getElementById('nes-up').addEventListener('touchend', function(event) {
    socket.emit('keyUp', {room: room, keyCode: 38});
});

document.getElementById('nes-down').addEventListener('touchstart', function(event) {
    socket.emit('keyDown', {room: room, keyCode: 40});
});

document.getElementById('nes-down').addEventListener('touchend', function(event) {
    socket.emit('keyUp', {room: room, keyCode: 40});
});

document.getElementById('nes-left').addEventListener('touchstart', function(event) {
    socket.emit('keyDown', {room: room, keyCode: 37});
});

document.getElementById('nes-left').addEventListener('touchend', function(event) {
    socket.emit('keyUp', {room: room, keyCode: 37});
});



document.getElementById('nes-right').addEventListener('touchstart', function(event) {
    socket.emit('keyDown', {room: room, keyCode: 39});
});

document.getElementById('nes-right').addEventListener('touchend', function(event) {
    socket.emit('keyUp', {room: room, keyCode: 39});
});



document.getElementById('nes-start').addEventListener('touchstart', function(event) {
    socket.emit('keyDown', {room: room, keyCode: 13});
});

document.getElementById('nes-start').addEventListener('touchend', function(event) {
    socket.emit('keyUp', {room: room, keyCode: 13});
});




document.getElementById('nes-select').addEventListener('touchstart', function(event) {
    socket.emit('keyDown', {room: room, keyCode: 17});
});

document.getElementById('nes-select').addEventListener('touchend', function(event) {
    socket.emit('keyUp', {room: room, keyCode: 17});
});
