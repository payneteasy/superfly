package org.springframework.core.type.classreading;

import org.springframework.asm.ClassReader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;

import com.payneteasy.superfly.client.classreading.MethodAnnotationMetadataSource;
import com.payneteasy.superfly.client.classreading.MethodReadingMetadataReader;

public class MultipleAnnotationValuesMetadataReader implements MethodReadingMetadataReader {
	
	private final ClassReader classReader;

	private final ClassLoader classLoader;

	public MultipleAnnotationValuesMetadataReader(ClassReader classReader,
			ClassLoader classLoader) {
		this.classReader = classReader;
		this.classLoader = classLoader;
	}
	
	public ClassMetadata getClassMetadata() {
		ClassMetadataReadingVisitor visitor = new ClassMetadataReadingVisitor();
		this.classReader.accept(visitor, true);
		return visitor;
	}
	
	public AnnotationMetadata getAnnotationMetadata() {
		MultipleValuesAnnotationMetadataReadingVisitor visitor = new MultipleValuesAnnotationMetadataReadingVisitor(this.classLoader, false);
		this.classReader.accept(visitor, true);
		return visitor;
	}

	public MethodAnnotationMetadataSource getMethodAnnotationMetadataSource() {
		MultipleValuesAnnotationMetadataReadingVisitor visitor = new MultipleValuesAnnotationMetadataReadingVisitor(this.classLoader, true);
		this.classReader.accept(visitor, true);
		return visitor;
	}

}
