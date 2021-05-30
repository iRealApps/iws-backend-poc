package iws.backend

import groovy.json.JsonOutput
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional

class BootStrap {

  GrailsApplication grailsApplication

  def init = { servletContext ->
    ensureGuestUserExists()
    if (!Utils.isTestEnvironment()) populateDataSet()
  }

  @Transactional
  def ensureGuestUserExists() {
    println "BEGIN BootStrap.ensureGuestUserExists()"
    String guestLoginName = grailsApplication.config.getProperty('grails.guest-loginname')
    String guestPassword = grailsApplication.config.getProperty('grails.guest-password')
    String guestName = grailsApplication.config.getProperty('grails.guest-name')
    User user = User.find { loginname == guestLoginName }
    if (!user) {
      println "BootStrap.ensureGuestUserExists() -> Creating Guest User"
      user = new User([loginname: guestLoginName, name: guestName])
      user.password = Utils.encryptString(guestPassword)
      user.createdAt = Utils.getNow()
      if (user.validate()) {
        user.save(flush: true)
      } else {
        println "BootStrap.ensureGuestUserExists() -> ERROR: ${user.errors.allErrors.join('|')}"
      }
    }
    println "END BootStrap.ensureGuestUserExists()"
  }

  @Transactional
  def populateDataSet() {
    println "BEGIN BootStrap.populateDataSet()"
    [
        start             : [
            isDefault: true,
            details  : [
                type          : "say",
                delayInSeconds: 5,
                message       : "Hello {name}, good {timeOfDay}!",
            ],
            action   : [
                type  : "goto",
                target: "greet",
            ],
        ],
        getInterest       : [
            details: [
                type   : "chooseOne",
                message: "What are you interested in?",
                options: [
                    [
                        name: "Technology Services",
                    ],
                    [
                        name: "Content",
                    ],
                ]
            ],
            actions: [
                type   : "choice",
                options: [
                    [
                        choice: "Technology Services",
                        action: [
                            type  : "goto",
                            target: "services",
                        ],
                    ],
                    [
                        choice: "Content",
                        action: [
                            type  : "goto",
                            target: "content",
                        ],
                    ],
                ],
            ],
        ],
        services          : [
            details: [
                type          : "say",
                delayInSeconds: 5,
                message       :
                    "If you are looking for innovations, then iRealities is the partner to align with.",
            ],
            action : [
                type  : "goto",
                target: "getIndustry",
            ],
        ],
        getIndustry       : [
            details: [
                type    : "ask",
                message : "May I know which industry you are from?",
                dataType: "industry",
            ],
            action : [
                type: "function",
                name: "handleIndustry",
            ],
        ],
        pharma            : [
            details: [
                type          : "say",
                delayInSeconds: 10,
                message       :
                    "Novartis, Pfizer, GSK, Bayer to Glemark and Sunpharma are users of iRealities Technology " +
                        "services. Other than corporate and product web sites, these companies have " +
                        "increased their customer engagement time using our content and technology " +
                        "solutions.",
            ],
            action : [
                type  : "goto",
                target: "showCaseStudies?",
            ],
        ],
        "showCaseStudies?": [
            details: [
                type   : "chooseOne",
                message: "Shall I take you to some case studies?",
                options: [
                    [
                        name: "Yes please.",
                    ],
                    [
                        name: "No, thank you.",
                    ]
                ]
            ],
            actions: [
                type   : "choice",
                options: [
                    [
                        name  : "Yes please.",
                        action: [
                            type  : "goto",
                            target: "caseStudies",
                        ],
                    ],
                    [
                        name  : "No, thank you.",
                        action: [
                            type  : "goto",
                            target: "bye",
                        ],
                    ],
                ],
            ],
        ],
        caseStudies       : [
            details: [
                type    : "document",
                message : "Pharma Case Studies",
                document: "https://irealities.com/digital-marketing.html",
            ],
            action : [
                type  : "goto",
                target: "bye",
            ],
        ],
        unknownIndustry   : [
            details: [
                type          : "say",
                delayInSeconds: 5,
                message       : "Unknown industry. TODO: ?",
            ],
            action : [
                type  : "goto",
                target: "getIndustry",
            ],
        ],
        content           : [
            details: [
                type          : "say",
                delayInSeconds: 5,
                message       : "TODO",
            ],
            action : [
                type  : "goto",
                target: "end",
            ],
        ],
        bye               : [
            details: [
                type          : "say",
                delayInSeconds: 5,
                message       : "Good bye!",
            ],
            action : [
                type  : "goto",
                target: "end",
            ],
        ],
    ].each { key, value ->
      println "BootStrap.populateDataSet() -> Creating step: ${key}"
      Step step = new Step()
      step.name = key
      step.isDefault = (value.isDefault == true)
      step.details = JsonOutput.toJson(value.details)
      step.action = JsonOutput.toJson(value.action)
      if (step.validate()) {
        step.save(flush: true)
      } else {
        println "BootStrap.populateDataSet() -> ERROR: ${step.errors.allErrors.join('|')}"
      }
    }
    Step.findAll().each {
      println "END BootStrap.populateDataSet() <- ${it.name}"
    }
    println "END BootStrap.populateDataSet()"
  }

  def destroy = {
  }

}
