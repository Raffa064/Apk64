package apk64;

public class Logger {
	public boolean debugEnabled  = true;
	public boolean warningEnabled  = true;
	public boolean errorEnabled  = true;
	
	public void setProfile(Profile profile) {
		debugEnabled = profile.debugEnabled;
		warningEnabled = profile.warningEnabled;
		errorEnabled = profile.errorEnabled;
	}
	
	private void log(String msg) {
		System.out.println(msg);
	}

	public void D(String msg) {
		if (debugEnabled) log(msg);
	}

	public void W(String msg) {
		if (warningEnabled) log(msg);
	}

	public void E(String msg) {
		if (errorEnabled) log(msg);
	}
	
	public static enum Profile {
		ALL(true, true, true),
		DEBUG_ONLY(true, false, false),
		WARNIG_ONLY(false, true, false),
		ERROR_ONLY(false, false, true),
		IMPORTANT(false, true, true);

		public Profile(boolean debugEnabled, boolean warningEnabled, boolean errorEnabled) {
			this.debugEnabled = debugEnabled;
			this.warningEnabled = warningEnabled;
			this.errorEnabled = errorEnabled;
		};
		
		public boolean debugEnabled;
		public boolean warningEnabled;
		public boolean errorEnabled;
	}
}
