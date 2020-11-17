const
    UUID = '00001101-0000-1000-8000-00805F9B34FB'

const server = new(require('bluetooth-serial-port')).BluetoothSerialPortServer()
const { exec } = require('child_process')
const key = __dirname + '/key.sh '


let previous = {

    ts: 0,
    x: 0,
    y: 0

}

server.on('data', function( buffer ) {

    let string = buffer.toString()
    let { ts, x, y, z} = JSON.parse(string.slice(0, string.indexOf('}') + 1))

    x = Math.round(x)
    y = Math.round(y)

    
    if (!previous.ts)

        previous = {

            ts: ts,
            x: x,
            y: y

        }

    if (previous.x > x) {
    
        console.log('up')
        exec(key + 'Up')
    
    } else if (previous.x < x) {

        exec(key + 'Down')

        console.log('down')
    }

    if (previous.y > y) {

        exec(key + 'Left')
        console.log('left')

    }

    else if (previous.y < y) {

        exec(key + 'Right')
        console.log('right')

    }

    previous = {ts: ts, x: x, y: y}
    

    

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
