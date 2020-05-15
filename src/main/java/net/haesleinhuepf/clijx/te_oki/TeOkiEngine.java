/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2020 ImageJ developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.haesleinhuepf.clijx.te_oki;

import ij.IJ;
import net.imagej.legacy.IJ1Helper;
import net.imagej.legacy.plugin.IJ1MacroEngine;
import org.scijava.ui.swing.script.TextEditor;

import javax.script.ScriptException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Robert Haase
 */
public class TeOkiEngine extends IJ1MacroEngine {

	public static String teOkiDirectory = "C:/structure/code/pyclesperanto_prototype/";
	public static String conda_directory = "C:\\Users\\rober\\Anaconda3";
	public static String conda_env = "te_oki";


	public TeOkiEngine(IJ1Helper ij1Helper) {
		super(ij1Helper);
	}

	@Override
	public Object eval(final String macro) throws ScriptException {

		new Thread(new Runnable() {
			@Override
			public void run() {

				boolean isWindows = System.getProperty("os.name")
						.toLowerCase().startsWith("windows");

				File directory = new File(teOkiDirectory);

				String conda_code;

				if (isWindows) {
					conda_code = "call " + conda_directory + "\\Scripts\\activate.bat " + conda_directory + "\n" +
							"call conda activate " + conda_env + "\n" +
							"cd " + directory + "\n" +
							"python temp.py";
				} else {
					conda_code = "conda activate " + conda_env + "\n" +
							"cd " + directory + "\n" +
							"python temp.py";
				}

				System.out.println(conda_code);

				Process process;
				try {

					Files.write(Paths.get(directory + "/temp.py"), macro.getBytes());
					if (isWindows) {
						System.out.println("Writing to " + directory + "/temp.bat");
						Files.write(Paths.get(directory + "/temp.bat"), conda_code.getBytes());
						process = Runtime.getRuntime()
								.exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + directory + "/temp.bat");
								//.exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + directory + "/temp.bat");
					} else {
						Files.write(Paths.get(directory + "/temp.sh"), conda_code.getBytes());
						process = Runtime.getRuntime()
								.exec(directory + "/temp.sh");
					}
					process.waitFor();
					System.out.println("Returned");

					byte[] error = new byte[1024];
					process.getErrorStream().read(error);
					IJ.log(new String(error));

				} catch (IOException e) {
					e.printStackTrace();
					//} catch (InterruptedException e) {
					//	e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				IJ.log("Bye.");
			}
		}).start();

		return null;
	}

	public static void main(String[] args) throws IOException, ScriptException {
		String content = new String(Files.readAllBytes(Paths.get(teOkiDirectory + "te_oki.py")));

		new TeOkiEngine(null).eval(content);
	}
}
