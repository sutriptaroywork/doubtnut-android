APPLICATION_ID = com.doubtnutapp
APPLICATION_ID_STAGING = com.doubtnutapp.staging
RUN_COMMAND = adb shell am start -n $(APPLICATION_ID)/$(APPLICATION_ID).ui.splash.SplashActivity
RUN_COMMAND_STAGING = adb shell am start -n $(APPLICATION_ID_STAGING)/$(APPLICATION_ID).ui.splash.SplashActivity

offline?=true
variant?=iD

ifeq ($(offline), true)
	OFFLINE_TOKEN= --offline
endif

ifeq ($(variant), iR)
	run_command=$(RUN_COMMAND)
else
	run_command=$(RUN_COMMAND_STAGING)
endif

clean:
	./gradlew $(OFFLINE_TOKEN) clean

install:
	./gradlew $(OFFLINE_TOKEN) $(variant) -x lint && $(run_command)
