package example.classpath.resourceLoader;

import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;

/**
 * author: sin
 * time: 2020/5/28 14:16
 */
public class SystemFileResourceLoaderExample {

	public static void main(String[] args) throws IOException {
		ResourceLoader resourceLoader = new FileSystemResourceLoader();
		Resource resource = resourceLoader.getResource("D:\\projects2\\loadAid-h5\\js\\common.js");
		System.err.println(resource.getClass());

		System.err.println(StringUtils.cleanPath("D:\\projects2\\loadAid-h5\\js\\common.js"));

		System.err.println(StringUtils.delimitedListToStringArray("1,2,3", ",", "2"));

		String[] arrs = new String[]{"aa", "bb", "cc", "dd"};
		System.err.println(StringUtils.collectionToDelimitedString(Arrays.asList(arrs), ",", "{", "}"));
		System.err.println(StringUtils.collectionToDelimitedString(Arrays.asList(arrs), "/", "", ""));

		System.err.println(System.getProperty("java.class.path"));

		System.err.println("PathMatchingResourcePatternResolver: 开始!");
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		for (Resource resolverResource : resolver.getResources("classpath*:/**/*.xml")) {
			System.err.println(resolverResource.getURI().getPath());
		}
	}
}
