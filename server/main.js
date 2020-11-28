'use strict'

const { exec } = require('child_process')

const nav = __dirname + '/nav.sh '
const down = __dirname + '/down.sh '
const up = __dirname + '/up.sh '
const key = __dirname + '/key.sh '
const focus = __dirname + '/focus.sh'

const variance = 14
const threshold = 4

let ts = 0
let cal = {

    x: 20,
    y: 0

}


const socket = () => {
        
    var app = require('http').createServer()
    var io = require('socket.io')(app)

    app.listen(3002)

    io.on('connection', client => { 

        client.on('data', message => {
            console.log(message)
            
            const { cmd, data } = JSON.parse(message)

            cmd && sensor[cmd](data)
            
        })

    })
}


const bluetooth = () => {

    const UUID = '00001101-0000-1000-8000-00805F9B34FB'
    const server = new(require('bluetooth-serial-port')).BluetoothSerialPortServer()


    server.on('data', function( buffer ) {


        const commands = buffer.toString().replace(/\}\{/g, '}~{').split('~')

        commands.map(a => {

            try {

                const { cmd, data } = JSON.parse(a)

                cmd && sensor[cmd](data)
                
            } catch(e) {

                console.log('bad packet')
            
            }
            
        })
        
    }.bind( this ) )

    server.on('disconnected', () => console.log('DISCONNECT'))

    server.on('closed', () => console.log('CLOSE'))

    server.on('failure', e => console.log('FAIL', e))

    server.listen(
        
        clientAddress => {

            ts = 0
            exec(focus)
            console.log('CONNECTED # MAC ' + clientAddress)
            
        },

        e => console.error('Server error:' + e),

        {
            uuid: UUID,
            channel: 1
        }

    )
}

const sensor = {

    nav: data => {

        let now = new Date(),
            delay = now - ts

        ts = now
    
        const [ x, y, z ] = data

        const angx = angle(x, z) - cal.x
        const angy = angle(y, z) - cal.y
        const dirx = (angx > 0 ? 'Down' : 'Up')
        const diry = (angy > 0 ? 'Right' : 'Left')

        joy(angx, dirx, delay)
        joy(angy, diry, delay)
        
    },
    
    key: data => exec(key + data),
    down: data => exec(down + data),
    up: data => exec(up + data),

    cal: data => {
        
        cal.x = angle(data[0], data[2])
        cal.y = angle(data[1], data[2])

    }

}


const angle = (a, b) => a / Math.sqrt(a * a + b * b) * 180 / Math.PI

async function joy(angle, dir, delay) {

    if (Math.abs(angle) > variance)

        exec(down + dir)

    else if (Math.abs(angle) > threshold) {

        const x = Math.abs(Math.min(variance, Math.abs(angle)))
        const fluct = Math.round(delay / 100 * (Math.pow(x, 3) / Math.sqrt(x)) / (Math.pow(variance, 3) / Math.sqrt(variance)) * 100)

        exec(nav + dir + ' ' + fluct / 1000)

    }

}


socket()