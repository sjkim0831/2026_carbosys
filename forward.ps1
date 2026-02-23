$wslIp = "172.26.168.161"
$ports = @(8761, 8888, 9000, 18003, 18004, 18010)

# 기존 포트포워딩 규칙 삭제
foreach ($port in $ports) {
    netsh interface portproxy delete v4tov4 listenport=$port listenaddress=0.0.0.0 2>$null
}

# 새 포트포워딩 규칙 추가
foreach ($port in $ports) {
    Write-Host "Forwarding port $port to ${wslIp}:$port"
    netsh interface portproxy add v4tov4 listenport=$port listenaddress=0.0.0.0 connectport=$port connectaddress=$wslIp
}
Write-Host "Port forwarding complete."
netsh interface portproxy show all
