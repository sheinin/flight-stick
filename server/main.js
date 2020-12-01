'use strict'

const { exec } = require('child_process')

const keys = {
    nav: __dirname + '/nav.sh ',
    down: __dirname + '/down.sh ',
    up: __dirname + '/up.sh ',
    key: __dirname + '/key.sh ',
    focus: __dirname + '/focus.sh'
}

const variance = 10
const threshold = 4


let prev

let cal = {

    x: 20,
    y: 0

}


const socket = () => {
        
    var app = require('http').createServer()
    var io = require('socket.io')(app)

    app.listen(3002)

    io.on('connection', client => { 
console.log('con')
        prev = {
            delay: 0,
            ts: 0,
            dirx: null,
            diry: null
        }
        exec(keys.focus)

        client.on('data', message => {
            
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
            prev = Date.now()
            exec(keys.focus)
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

        prev.ts = prev.ts || Date.now()

        const now = Date.now()
        
        let delay = now - prev.ts

        const [ x, y, z ] = data

        const angx = angle(x, z) - cal.x
        const angy = angle(y, z) - cal.y
        const dirx = (angx > 0 ? 'Down' : 'Up')
        const diry = (angy > 0 ? 'Right' : 'Left')

        if (prev.ts + prev.delay > now) {

            const diff = prev.ts + prev.delay - now
            
            
            if (delay - diff < 1) {
                exec(keys.up + prev.dirx)
                exec(keys.up + prev.diry)
                
            } else 

                delay -= diff
        }

        joy(angx, dirx, delay)
        joy(angy, diry, delay)

        prev = {
            ts: now,
            delay: delay,
            dirx: dirx,
            diry: diry
        }
        
    },
    
    key: data => exec(keys.key + data),
    down: data => exec(keys.down + data),
    up: data => exec(keys.up + data),

    cal: data => {
        
        cal.x = angle(data[0], data[2])
        cal.y = angle(data[1], data[2])

    }

}


const angle = (a, b) => a / Math.sqrt(a * a + b * b) * 180 / Math.PI

async function joy(angle, dir, delay) {


    if (Math.abs(angle) > variance)

        exec(keys.down + dir)

    else if (Math.abs(angle) > threshold) {

        
        const x = Math.abs(Math.min(variance, Math.abs(angle)))

        delay = delay * ((Math.pow(x, 5) / Math.sqrt(x)) / (Math.pow(variance, 5) / Math.sqrt(variance)))
        ///delay += Math.sin(Math.pi*(x / variance))
        delay /= 1000
        
        exec(keys.nav + dir + ' ' + delay)

    }

}


socket()


///sin((pi)×(abs(x)/10)) + 50 * ((pow(abs(x), 5) / sqrt(abs(x))) / (pow(10, 5) / sqrt(10)))