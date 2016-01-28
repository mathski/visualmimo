var socket = io();

var room = 0;

socket.emit('joinRoom', {room: room});

$('#nes-a').mousedown(function() {
    socket.emit('keyDown', {room: room, keyCode: 88});
});

$('#nes-a').mouseup(function() {
    socket.emit('keyUp', {room: room, keyCode: 88});
});


$('#nes-b').mousedown(function() {
    socket.emit('keyDown', {room: room, keyCode: 90});
});

$('#nes-b').mouseup(function() {
    socket.emit('keyUp', {room: room, keyCode: 90});
});

$('#nes-up').mousedown(function() {
    socket.emit('keyDown', {room: room, keyCode: 38});
});

$('#nes-up').mouseup(function() {
    socket.emit('keyUp', {room: room, keyCode: 38});
});

$('#nes-down').mousedown(function() {
    socket.emit('keyDown', {room: room, keyCode: 40});
});

$('#nes-down').mouseup(function() {
    socket.emit('keyUp', {room: room, keyCode: 40});
});

$('#nes-left').mousedown(function() {
    socket.emit('keyDown', {room: room, keyCode: 37});
});

$('#nes-left').mouseup(function() {
    socket.emit('keyUp', {room: room, keyCode: 37});
});



$('#nes-right').mousedown(function() {
    socket.emit('keyDown', {room: room, keyCode: 39});
});

$('#nes-right').mouseup(function() {
    socket.emit('keyUp', {room: room, keyCode: 39});
});



$('#nes-start').mousedown(function() {
    socket.emit('keyDown', {room: room, keyCode: 13});
});

$('#nes-start').mouseup(function() {
    socket.emit('keyUp', {room: room, keyCode: 13});
});




$('#nes-select').mousedown(function() {
    socket.emit('keyDown', {room: room, keyCode: 17});
});

$('#nes-select').mouseup(function() {
    socket.emit('keyUp', {room: room, keyCode: 17});
});
