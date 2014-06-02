#include <stdio.h>
#include <string.h>
#include <jvmti.h>

static jrawMonitorID lock;
static FILE* out;

void JNICALL MethodEntry(jvmtiEnv *jvmti, JNIEnv* jni_env, jthread thread, jmethodID method) {
	static int flag = 0;
	char *method_name, *class_signature;
	jclass class;

	(*jvmti)->RawMonitorEnter(jvmti, lock);

	(*jvmti)->GetMethodName(jvmti, method, &method_name, NULL, NULL);
	(*jvmti)->GetMethodDeclaringClass(jvmti, method, &class);
	(*jvmti)->GetClassSignature(jvmti, class, &class_signature, NULL);

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

	(*jvmti)->RawMonitorExit(jvmti, lock);
}

const jvmtiEventCallbacks callbacks = {
	.MethodEntry = &MethodEntry
};


JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *jvm, char *options, void *reserved) {
	jvmtiEnv *jvmti = NULL;
	jvmtiCapabilities capability;

	(*jvm)->GetEnv(jvm, (void **) &jvmti, JVMTI_VERSION);
	(*jvmti)->CreateRawMonitor(jvmti, "Method Call Monitor", &lock);
	
	(*jvmti)->GetCapabilities(jvmti, &capability);
	capability.can_generate_method_entry_events = 1;
	(*jvmti)->AddCapabilities(jvmti, &capability);

	(*jvmti)->SetEventCallbacks(jvmti, &callbacks, sizeof(callbacks));
	(*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_METHOD_ENTRY, NULL);

	return JNI_OK;
}