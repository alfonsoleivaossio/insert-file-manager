package net.excelsys.delivery.filemanager;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

public class DriveTypeInfo {

	public static void main(String[] args) {
		System.out
				.println("File system roots returned by   FileSystemView.getFileSystemView():");
		FileSystemView fsv = FileSystemView.getFileSystemView();
		File[] roots = fsv.getFiles(new File("/media"), false);
		for (int i = 0; i < roots.length; i++) {
			if (!roots[i].getPath().equals("/media/cdrom")) {
				System.out.println("Root: " + roots[i]);
				System.out.println("Drive: " + roots[i]);
				System.out.println("Display name: "
						+ fsv.getSystemDisplayName(roots[i]));
				System.out.println("Is drive: " + fsv.isDrive(roots[i]));
				System.out.println("Is floppy: " + fsv.isFloppyDrive(roots[i]));
				System.out.println("Is empty: "
						+ (fsv.getFiles(roots[i], false).length == 0 ? "True"
								: "False"));
				System.out.println("Is filesystem: "
						+ fsv.isFileSystem(roots[i]));
				System.out.println("Readable: " + roots[i].canRead());
				System.out.println("Writable: " + roots[i].canWrite());
			}
		}

		System.out.println("Home directory: " + fsv.getHomeDirectory());

		System.out.println("File system roots returned by File.listRoots():");

		// File[] f = File.listRoots();
		// for (int i = 0; i < f.length; i++) {
		// System.out.println("Drive: " + f[i]);
		// System.out.println("Display name: "
		// + fsv.getSystemDisplayName(f[i]));
		// System.out.println("Is drive: " + fsv.isDrive(f[i]));
		// System.out.println("Is floppy: " + fsv.isFloppyDrive(f[i]));
		// System.out.println("Readable: " + f[i].canRead());
		// System.out.println("Writable: " + f[i].canWrite());
		// }

	}
}