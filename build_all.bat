@echo off
call gradlew core:shadowJar backend-rpc-core:jar rewind_v1_5:jar eaglermotd:jar eaglerweb:jar plan:jar supervisor-core:jar
copy "core\build\libs\EaglerXServer.jar" "EaglerXServer.jar"
copy "backend-rpc-core\build\libs\EaglerXBackendRPC.jar" "EaglerXBackendRPC.jar"
copy "rewind_v1_5\build\libs\EaglerXRewind.jar" "EaglerXRewind.jar"
copy "eaglermotd\build\libs\EaglerMOTD.jar" "EaglerMOTD.jar"
copy "eaglerweb\build\libs\EaglerWeb.jar" "EaglerWeb.jar"
copy "plan\build\libs\EaglerXPlan.jar" "EaglerXPlan.jar"
copy "supervisor-core\build\libs\EaglerXSupervisor.jar" "EaglerXSupervisor.jar"
pause
