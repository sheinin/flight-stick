WINDID=`xdotool search --name "DOSBox" | tail -1`
xdotool windowactivate --sync $WINDID
sleep 0.2
./key.sh