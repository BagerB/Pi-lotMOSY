install
	sudo cp ./pi-lot_tx.service /etc/systemd/system/
	sudo cp ./mjpg_streamer.service /etc/systemd/system/

start
	sudo systemctl daemon-reload
	sudo systemctl enable pi-lot_tx.service
	sudo systemctl enable mjpg_streamer.service
	sudo systemctl start pi-lot_tx.service
	sudo systemctl start mjpg_streamer.service