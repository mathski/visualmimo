var express = require('express');
var app = express();
app.use(express.static(__dirname));
var http = require('http').Server(app);
var io = require('socket.io')(http);

var rooms = 0;

app.get('/', function(req, res){
    res.sendFile(__dirname + '/index.html');
});

app.get('/controller', function(req, res){
    res.sendFile(__dirname + '/controller.html');
});

io.on('connection', function(socket){
    console.log('user connect at %s', socket.id);

    socket.on('joinRoom', function(data) {
        console.log('joinRoom');
        if (!!data.room) {
            socket.join(data.room);
        } else {
            socket.join(rooms);
        }
    });

    socket.on('keyDown', function(data) {
        io.to(data.room).emit('keyDown', {keyCode: data.keyCode});
    });

    socket.on('keyUp', function(data) {
        io.to(data.room).emit('keyUp', {keyCode: data.keyCode});
    });
});


http.listen(3000, function(){
    console.log('listening on *:3000');
});
