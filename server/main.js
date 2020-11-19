const
    UUID = '00001101-0000-1000-8000-00805F9B34FB'

const server = new(require('bluetooth-serial-port')).BluetoothSerialPortServer()
const { exec } = require('child_process')
const key = __dirname + '/key.sh '

const delay = 38
const variance = 21

let previous = {

    ts: 0,
    x: 0,
    y: 0

}

server.on('data', function( buffer ) {

    const commands = buffer.toString().replace(/\}\{/g, '}~{').split('~')

    commands.map(a => {

        try {

            const { cmd, data } = JSON.parse(a)

            sensor[cmd](data)

        } catch(e) {

            console.log('bad packet')
        
        }
        
    })
    
}.bind( this ) )

server.on('disconnected', ()=>console.log('DISCONNECT'))
server.on('closed', ()=>console.log('CLOSE'))
server.on('failure', (e)=>console.log('FAIL',e))

server.listen(function (clientAddress) {

    console.log('CONNECTED # MAC ' + clientAddress)

}, function(error) {

    console.error('Server error:' + error)

}, {uuid: UUID, channel: 1 })


const sensor = {

    xyz: data => {

        const { ts, x, y, z } = data
        const ax = x / Math.sqrt(x * x + z * z) * 180 / Math.PI - 20
        const ay = y / Math.sqrt(y * y + z * z) * 180 / Math.PI

        let dx = Math.round(delay / 100 * (ax > 0 ? Math.min(variance, ax) : Math.max(variance * -1, ax) * -1) / variance * 100)
        let dy = Math.round(delay / 100 * (ay > 0 ? Math.min(variance, ay) : Math.max(variance * -1, ay) * -1) / variance * 100)

        dx = dx - Math.pow(1 / dx, 5)
        dy = dy - Math.pow(1 / dy, 5)

        const ud = (ax > 0 ? 'Down' : 'Up')
        const lr = (ay > 0 ? 'Right' : 'Left')

        if (ax > variance || ax < variance * -1)

            exec(__dirname + '/down.sh ' + ud)

        else

            exec(key + ud + ' ' + dx / 1000)


        if (ay > variance || ay < variance * -1)

            exec(__dirname + '/down.sh ' + lr)

        else

            exec(key + lr + ' ' + dy / 1000)

        console.log(ud + ': ' + ax, lr +': ' + ay)

    },
    
    key: data => {

        console.log(data)

    }

}