/**
 *
 */
package com.publiclibs.networkutils.info2bytes;

import static com.publiclibs.networkutils.info2bytes.InetAddressHeader.ipv4;
import static com.publiclibs.networkutils.info2bytes.InetAddressHeader.ipv6;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * @author freedom1b2830
 * @date 2023-апреля-22 14:04:03
 */
public final class InetAddressSerilization {
	/**
	 * @param inetAddress
	 * @return
	 */
	private static byte getFAM(final InetAddress inetAddress) {
		// FAM
		final Class<? extends InetAddress> classs = inetAddress.getClass();
		if (classs == Inet4Address.class) {
			return ipv4;
		} else if (classs == Inet6Address.class) {
			return ipv6;
		} else {
			throw new UnsupportedOperationException(classs.getSimpleName());
		}
	}

	public static void main(final String[] args) throws IOException {
		final ArrayList<InetAddress> completeList = new ArrayList<>();
		final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			final NetworkInterface networkInterface = interfaces.nextElement();
			final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
			while (inetAddresses.hasMoreElements()) {
				final InetAddress addr = inetAddresses.nextElement();
				completeList.add(addr);
			}
		}
		final ByteArrayOutputStream baos = serializeListOfInetAddressToByteArrayOutputStream(completeList);
		final byte[] bytes = serializeListOfInetAddressToBytes(completeList);
		System.err.println(Arrays.toString(bytes));
		System.err.println(Arrays.toString(baos.toByteArray()));

	}

	/**
	 * @param inetAddresses
	 * @return
	 * @throws IOException
	 */
	public static ByteArrayOutputStream serializeListOfInetAddressToByteArrayOutputStream(
			final List<InetAddress> inetAddresses) throws IOException {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byteArrayOutputStream.write(inetAddresses.size());
		for (int i = 0; i < inetAddresses.size(); i++) {
			final InetAddress inetAddress = inetAddresses.get(i);
			serializeSingleInetAddressToByteArrayOutputStream(byteArrayOutputStream, inetAddress);
		}
		return byteArrayOutputStream;
	}

	/**
	 * @param completeList
	 * @return
	 * @throws IOException
	 */
	public static byte[] serializeListOfInetAddressToBytes(final List<InetAddress> completeList) throws IOException {
		return serializeListOfInetAddressToByteArrayOutputStream(completeList).toByteArray();
	}

	public static ByteArrayOutputStream serializeSingleInetAddressToByteArrayOutputStream(
			final ByteArrayOutputStream result, final InetAddress inetAddress) throws IOException {
		writeFAM(result, inetAddress);
		result.write(inetAddress.getAddress());
		return result;
	}

	public static ByteArrayOutputStream serializeSingleInetAddressToByteArrayOutputStream(final InetAddress inetAddress)
			throws IOException {
		return serializeSingleInetAddressToByteArrayOutputStream(new ByteArrayOutputStream(), inetAddress);
	}

	public static byte[] serializeSingleInetAddressToBytes(final InetAddress inetAddress) throws IOException {
		return serializeSingleInetAddressToByteArrayOutputStream(inetAddress).toByteArray();
	}

	/**
	 * @param result
	 * @param inetAddress
	 */
	public static void writeFAM(final ByteArrayOutputStream result, final InetAddress inetAddress) {
		final byte fam = getFAM(inetAddress);
		result.write(fam);
	}

	private InetAddressSerilization() {
	}
}
