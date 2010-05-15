/************************************************************************
 MIT License

 Copyright (c) 2010 University of Connecticut

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
***********************************************************************/

package edu.uconn.vstlf.shutil;

public class Run {

	static String _USAGE = 
	
		"USAGE:\n\tjava -jar uconn-vstlf.jar [command [cmd ... spcfc ... args ...]]\n\n\t" +
	
		"Issue a specific command with no arguments for detailed usage information\n\n\t" +
		
		"Valid commands are:\n\t" +
		"train\t\tUse historical load data to generate neural nets that can be used in real-time VSTLF\n\t" +
		"validate\tUse historical load data to test the quality of networks trained using the 'train' command\n\t" +
		"run\t\tRun the real-time VSTLF system (headless.  output through xml stream)\n\t" +
		"run-gui\t\tRun the real-time VSTLF system (requires this machine to support graphics via Java Swing)\n\t" +		
		"audit\t\tGet a report on the error results of a previously run (or currently running) real-time system\n\t" + 		
		"reset\t\tErase the current directory's VSTLF history\n\t" +		
		"build-perst\tBuild a perst database from xml files\n\t" + 		
		"gen5m\t\tGenerate 5 minute loads from a perst database containing 4 second loads\n\n";
		
	
	public static void main(String[] args) {
		if(args.length==0){
			System.out.println(_USAGE);
			System.exit(0);
		}
		String cmd = args[0];
		String[] cargs = new String[args.length-1];
		System.arraycopy(args, 1, cargs, 0, cargs.length);
		if(cmd.equals("train")){
			RunTraining.main(cargs);
		}
		else if(cmd.equals("validate")){
			RunValidation.main(cargs);
		}
		else if(cmd.equals("run")){
			RunHeadless.main(cargs);
		}
		else if(cmd.equals("run-gui")){
			RunGUI.main(cargs);
		}
		else if(cmd.equals("audit")){
			RunAudition.main(cargs);
		}
		else if(cmd.equals("reset")){
			Reset.main(null);
		}
		else if(cmd.equals("build-perst")){
			RunFromXMLToPerst.main(cargs);
		}
		else if(cmd.equals("gen5m")) {
			RunGenerate5M.main(cargs);
		}
		else{
			System.out.println(_USAGE);
		}
	}

}
