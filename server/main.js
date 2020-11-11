const
    UUID = '00001101-0000-1000-8000-00805F9B34FB'

const server = new(require('bluetooth-serial-port')).BluetoothSerialPortServer()
//const robot = require("kbm-robot")
//var robot1 = require("robotjs");
//robot.startJar()

 
//const { keyboard, Key, mouse, left, right, up, down, screen } = require("@nut-tree/nut-js");
 


//keyboard.config.autoDelayMs = 0;

   

const sendkeys = require('sendkeys-js')

// for mac
const { exec } = require('child_process');


let buffer = []

let previous = {

    x: 0,
    y: 0

}

server.on('data', function( buffer ) {

    const
        incoming = buffer.toString().trim(),//.replace(/\;$/, ''),
        command = incoming.split(';')[0].split(':'),
        ts = command[0],
        coord = command[1].split(','),
        x = Math.round(coord[0]),
        y = Math.round(coord[1])

        //await keyboard.pressKey(Key.LeftSuper);
  //      keyboard.pressKey(Key.Space);

        //robot1.keyTap('enter')
//        keyboard.type("v");

    if (previous.x > x) {
        //sendkeys.send('c')
        sendkeys.send('space')
        //robot.press('r')
       
console.log('up')
exec('/home/hyptos/Repo/accelerometer-stick/key.sh')    
        exec('xdotool key Up')//, (err, stdout, stderr) => {
         //   console.log(stdout)
           // if (err) {
             // console.log(err)
             // return
            //}})

    } else if (previous.x < x) {


      //  robot.press('down')
        //    .sleep(100)

        console.log('down')
    }

    if (previous.y > y) {

        console.log('left')


  //      robot.press('`')
    //        .sleep(100)

    }

    else if (previous.y < y) {


    //    robot.press('right')
      //      .sleep(100)
        console.log('right')

    }

    previous = {x: x, y: y}
    

    

//    console.log('IN: ' + incoming)

    
}.bind( this ) )

server.on('disconnected', ()=>console.log('DISCONNECT'))
server.on('closed', ()=>console.log('CLOSE'))
server.on('failure', (e)=>console.log('FAIL',e))

server.listen(function (clientAddress) {

    console.log('CONNECTED # MAC ' + clientAddress)

}, function(error) {

    console.error('Server error:' + error)

}, {uuid: UUID, channel: 1 })
