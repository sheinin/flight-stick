const
    UUID = '00001101-0000-1000-8000-00805F9B34FB'

const server = new(require('bluetooth-serial-port')).BluetoothSerialPortServer()

server.on('data', function( buffer ) {

    const
        incoming = buffer.toString().trim(),
        commands = incoming.split( '\n' )

    console.log('IN: ' + incoming)

    
}.bind( this ) )

server.on('disconnected', ()=>console.log('DISCONNECT'))
server.on('closed', ()=>console.log('CLOSE'))
server.on('failure', (e)=>console.log('FAIL',e))

server.listen(function (clientAddress) {

    console.log('CONNECTED # MAC ' + clientAddress)

}, function(error) {

    console.error('Server error:' + error)

}, {uuid: UUID, channel: 1 })
