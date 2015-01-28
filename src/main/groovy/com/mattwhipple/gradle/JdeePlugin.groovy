package com.mattwhipple.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class JdeePlugin implements Plugin<Project> {

  def prj = { project ->

    "(jde-project-file-version" (["1.0"])
    "(jde-set-variables" { 
      "'(jde-compile-option-directory" ([project.sourceSets.main.output.classesDir])
      "'(jde-junit-working-directory" ([project.projectDir])
    
      "'(jde-compile-option-source" { 
	"'(" (["default"])
      }

      "'(jde-compile-option-target" { 
	"'(" (["default"])
      }

      "'(jde-compile-option-command-line-args" { 
	"'(" (["-${project.sourceCompatibility}"])
      }
    
      "'(jde-sourcepath" { 
	"'(" (
	  project.sourceSets.main.allSource.srcDirs 
	  + project.sourceSets.test.allSource.srcDirs)
      }
    
      "'(jde-global-classpath" { 
	"'(" (
	  [] + project.sourceSets.main.output.classesDir
	  + project.sourceSets.test.output.classesDir
	  + project.sourceSets.main.allSource.srcDirs
	  + project.sourceSets.test.allSource.srcDirs
	  + (([] as Set) + project.configurations.compile.getFiles() 
	     + project.configurations.testCompile.getFiles()))
      }
    }
  }

  @Override
  void apply(Project project) {

    project.task("jdee") << { 
      def output = new File(project.projectDir, "prj.el").newPrintWriter()
      try { 

	prj.delegate = new NodeBuilder() { 
	  def lev = 0
          
	  def write = { Object file -> 
	    output.print '\n' + ''.padRight(lev, ' ') +  "\"${file}\"".tr('\\', '/') 
	  }

	  Object createNode(Object name) { 
	    output.print '\n'  + ''.padRight(lev++, ' ') + name
	    return name
	  }
  
	  Object createNode(Object name, Object value) { 
	    createNode(name)
	    value.each write
	    return name
	  }

	  void nodeCompleted(Object parent, Object child) { 
	    output.print ")"
	    lev--
	  }
	}
	prj(project)
	output.close()
      } finally { 
	output.flush()
      }
    }
  }

}