#include <stdio.h>
#include <string.h>
#include <jvmti.h>

static jrawMonitorID lock;
static FILE* out;

void JNICALL MethodEntry(jvmtiEnv *jvmti, JNIEnv* jni_env, jthread thread, jmethodID method) {
	static int flag = 0;
	char *method_name, *class_signature;
	jclass class;

	if ((*jvmti)->RawMonitorEnter(jvmti, lock) == JVMTI_ERROR_NONE) {
		if ((*jvmti)->GetMethodName(jvmti, method, &method_name, NULL, NULL) == JVMTI_ERROR_NONE) {
			if ((*jvmti)->GetMethodDeclaringClass(jvmti, method, &class) == JVMTI_ERROR_NONE) {		
				if ((*jvmti)->GetClassSignature(jvmti, class, &class_signature, NULL) == JVMTI_ERROR_NONE) {
					if (strcmp(method_name, "startFlag") == 0 && strcmp(class_signature, "Lorg/scalameter/Executor$Measurer$MethodCall;") == 0) {
						flag = 1;
						out = fopen("MethodCallDump", "w");
					}

					if (flag) {
						fputs(class_signature, out);
						fputs(method_name, out);
						fputc('\n', out);
					}
					
					if (strcmp(method_name, "endFlag") == 0 && strcmp(class_signature, "Lorg/scalameter/Executor$Measurer$MethodCall;") == 0) {
						flag = 0;
						fflush(out);
						fclose(out);
					}

					(*jvmti)->Deallocate(jvmti, class_signature);
				}
			}

			(*jvmti)->Deallocate(jvmti, method_name);
		}

		(*jvmti)->RawMonitorExit(jvmti, lock);
	}
}

const jvmtiEventCallbacks callbacks = {
	.MethodEntry = &MethodEntry
};


JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *jvm, char *options, void *reserved) {
	jvmtiEnv *jvmti = NULL;
	jvmtiCapabilities capability;

	if ((*jvm)->GetEnv(jvm, (void **) &jvmti, JVMTI_VERSION) != JNI_OK) {
		fprintf(stderr, "Fail to get JVMTI environment");
		return JNI_ERR;
	}

	if ((*jvmti)->CreateRawMonitor(jvmti, "Method Call Monitor", &lock) != JVMTI_ERROR_NONE) {
		fprintf(stderr, "Fail to create raw monitor");
		return JNI_ERR;
	}
	
	if ((*jvmti)->GetCapabilities(jvmti, &capability) != JVMTI_ERROR_NONE) {
		fprintf(stderr, "Fail to get capabilities");
		return JNI_ERR;
	}

	capability.can_generate_method_entry_events = 1;
	
	if ((*jvmti)->AddCapabilities(jvmti, &capability) != JVMTI_ERROR_NONE) {
		fprintf(stderr, "Fail to add capabilities");
		return JNI_ERR;
	}

	if ((*jvmti)->SetEventCallbacks(jvmti, &callbacks, sizeof(callbacks)) != JVMTI_ERROR_NONE) {
		fprintf(stderr, "Fail to set event callbacks");
		return JNI_ERR;
	}

	if ((*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_METHOD_ENTRY, NULL)) {
		fprintf(stderr, "Fail to set event notification mode");
		return JNI_ERR;
	}

	return JNI_OK;
}