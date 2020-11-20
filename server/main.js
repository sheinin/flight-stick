const UUID = '00001101-0000-1000-8000-00805F9B34FB'

const server = new(require('bluetooth-serial-port')).BluetoothSerialPortServer()
const { exec } = require('child_process')
const nav = __dirname + '/nav.sh '
const down = __dirname + '/down.sh '
const up = __dirname + '/up.sh '
const key = __dirname + '/key.sh '
const focus = __dirname + '/key.sh '

const variance = 21
const threshold = 8
const alpha = 0.25
const lowpass = false

let xyz
let ts = 0

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
server.on('closed', ()=> {

    ts = 0
    console.log('CLOSE')

})
server.on('failure', (e)=>console.log('FAIL',e))

server.listen(clientAddress => {

    console.log('CONNECTED # MAC ' + clientAddress)
    exec(focus)

}, function(error) {

    console.error('Server error:' + error)

}, {uuid: UUID, channel: 1 })


const sensor = {

    xyz: data => {

        let now = new Date()

        let delay = now - ts

        ts = now
    
        if (!xyz || !lowpass)

            xyz = data

        else

            for (let i = 0, ln = data.length; i < ln; i += 1 ) {

                xyz[i] = xyz[i] + alpha * (data[i] - xyz[i])

            }
    
        const [ x, y, z ] = xyz || data

        const ax = x / Math.sqrt(x * x + z * z) * 180 / Math.PI - 20
        const ay = y / Math.sqrt(y * y + z * z) * 180 / Math.PI

        let dx = Math.round(delay / 100 * (ax > 0 ? Math.min(variance, ax) : Math.max(variance * -1, ax) * -1) / variance * 100)
        let dy = Math.round(delay / 100 * (ay > 0 ? Math.min(variance, ay) : Math.max(variance * -1, ay) * -1) / variance * 100)

        dx = dx - Math.pow(1 / dx, 5)
        dy = dy - Math.pow(1 / dy, 5)

        const ud = (ax > 0 ? 'Down' : 'Up')
        const lr = (ay > 0 ? 'Right' : 'Left')

        if (ax > variance || ax < variance * -1)

            exec(down + ud)

        else if (dx > threshold)

            exec(nav + ud + ' ' + dx / 1000)


        if (ay > variance || ay < variance * -1)

            exec(down + lr)

        else if (dy > threshold)

            exec(nav + lr + ' ' + dy / 1000)

        //console.log(ud + ': ' + ax, lr +': ' + ay)

    },
    
    key: data => exec(key + data),
    down: data => exec(down + data),
    up: data => exec(up + data),

}